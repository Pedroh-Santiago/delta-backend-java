package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.TermoAdesaoNaoEncontrado;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosGetPolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosGetPolicyResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalData;
import feign.Request;
import feign.codec.DecodeException;
import org.junit.jupiter.api.Test;

class TermoAdesaoRegenerationServiceTest {

    private final TermoAdesaoReferenceRepository referenceRepository = mock(TermoAdesaoReferenceRepository.class);
    private final HeroSegurosGetPolicyClient getPolicyClient = mock(HeroSegurosGetPolicyClient.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final TermoAdesaoPdfService termoAdesaoPdfService = mock(TermoAdesaoPdfService.class);

    private final TermoAdesaoRegenerationService service = new TermoAdesaoRegenerationService(
        referenceRepository, getPolicyClient, tokenService, termoAdesaoPdfService
    );

    private final TermoAdesaoReference referencia = new TermoAdesaoReference(
        UUID.randomUUID(), "TCK-1", 148030, Convenio.CLT, "148030", Instant.now()
    );

    @Test
    void ticketEncontradoBuscaAPropostaNaHeroERegeneraOPdf() {
        HeroSegurosProposalData proposal = new HeroSegurosProposalData(
            148030, null, null, null, null, null, null, null, null, Collections.emptyList(), null, null, null
        );
        byte[] pdf = "pdf-bytes".getBytes();
        when(referenceRepository.findByTicket("TCK-1")).thenReturn(referencia);
        when(tokenService.getToken(Convenio.CLT)).thenReturn("token-hero");
        when(getPolicyClient.getProposal("Bearer token-hero", 148030))
            .thenReturn(new HeroSegurosGetPolicyResponse(proposal));
        when(termoAdesaoPdfService.generateFromProposal(proposal, "TCK-1", "148030")).thenReturn(pdf);

        byte[] resultado = service.regenerate("TCK-1");

        assertArrayEquals(pdf, resultado);
    }

    @Test
    void ticketNaoEncontradoLancaTermoAdesaoNaoEncontrado() {
        when(referenceRepository.findByTicket("TCK-desconhecido")).thenReturn(null);

        assertThrows(TermoAdesaoNaoEncontrado.class, () -> service.regenerate("TCK-desconhecido"));
        verify(tokenService, never()).getToken(any());
    }

    @Test
    void erroDoFeignNaBuscaViraHeroSegurosInvalidResponse() {
        when(referenceRepository.findByTicket("TCK-1")).thenReturn(referencia);
        when(tokenService.getToken(Convenio.CLT)).thenReturn("token-hero");
        when(getPolicyClient.getProposal(any(), eq(148030))).thenThrow(feignDecodeException());

        assertThrows(HeroSegurosInvalidResponse.class, () -> service.regenerate("TCK-1"));
    }

    private DecodeException feignDecodeException() {
        Request request = Request.create(
            Request.HttpMethod.GET, "https://hero/api/prestamista/proposal/148030",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, null
        );
        return new DecodeException(200, "corpo inesperado", request);
    }
}
