package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification;
import br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument.SendPolicyDocumentCommand;
import br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument.SendPolicyDocumentToSWorksUseCase;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosIssuePolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosQuotationClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessCreator;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessSettings;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCoverage;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicy;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyData;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationPlan;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksCreateProcessResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksInputField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IssuePolicyUseCaseSWorksTest {

    private final HeroSegurosIssuePolicyClient issuePolicyClient = mock(HeroSegurosIssuePolicyClient.class);
    private final HeroSegurosQuotationClient quotationClient = mock(HeroSegurosQuotationClient.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final TermoAdesaoPdfService termoAdesaoPdfService = mock(TermoAdesaoPdfService.class);
    private final TermoAdesaoReferenceRepository termoAdesaoReferenceRepository = mock(TermoAdesaoReferenceRepository.class);
    private final SendPolicyDocumentToSWorksUseCase sworks = mock(SendPolicyDocumentToSWorksUseCase.class);

    private final PrestamistaPolicyProperties properties = new PrestamistaPolicyProperties(Map.of("clt", 9));

    private final SWorksProcessCreator processCreator = mock(SWorksProcessCreator.class);

    private final String baseUrl = "http://local";

    private final IssuePolicyUseCase useCase = new IssuePolicyUseCase(
        issuePolicyClient, quotationClient, tokenService, properties, termoAdesaoPdfService,
        termoAdesaoReferenceRepository, sworks, processCreator, baseUrl
    );

    private final String ticket = "TCK-4477";
    private final String termoAdesaoUrlEsperado = baseUrl + "/heroseguros/prestamista/policies/" + ticket + "/termo-adesao";

    @BeforeEach
    void setUp() {
        when(tokenService.getToken(any())).thenReturn("token-hero");
        when(quotationClient.quote(any(), any())).thenReturn(quotacao());
        when(issuePolicyClient.issuePolicy(any(), any())).thenReturn(apolice());
        when(termoAdesaoPdfService.generate(any(), any())).thenReturn("pdf-bytes".getBytes());
        when(processCreator.habilitado()).thenReturn(false);
        when(processCreator.settings()).thenReturn(new SWorksProcessSettings(null, null, null));
    }

    @Test
    void criaOProcessoQuandoOIdentificadorNaoVemEACriacaoEstaHabilitada() {
        when(processCreator.habilitado()).thenReturn(true);
        when(processCreator.create(any())).thenReturn(new SWorksCreateProcessResponse("proc-criado", 2001));
        ArgumentCaptor<SendPolicyDocumentCommand> comando = ArgumentCaptor.forClass(SendPolicyDocumentCommand.class);
        when(sworks.execute(comando.capture())).thenReturn(sucesso());

        IssuePolicyResponse resposta = useCase.execute(request(null, "EXT-99"));

        assertEquals("proc-criado", comando.getValue().identificadorProcesso());
        assertEquals("SUCESSO", resposta.sworksStatus());
    }

    @Test
    void naoCriaProcessoQuandoOIdentificadorJaVemNoRequest() {
        when(processCreator.habilitado()).thenReturn(true);
        when(sworks.execute(any())).thenReturn(sucesso());

        useCase.execute(request("proc-informado", "EXT-99"));

        verify(processCreator, never()).create(any());
    }

    @Test
    void falhaAoCriarOProcessoNaoDerrubaAEmissao() {
        when(processCreator.habilitado()).thenReturn(true);
        when(processCreator.create(any())).thenThrow(new IllegalStateException("workflow inexistente"));

        IssuePolicyResponse resposta = useCase.execute(request(null, "EXT-99"));

        assertEquals(ticket, resposta.ticket());
        assertEquals(termoAdesaoUrlEsperado, resposta.termoAdesaoUrl());
        assertNull(resposta.sworksStatus());
        verify(sworks, never()).execute(any());
    }

    @Test
    void montaOsDadosEntradaConvertendoOsFormatos() {
        when(processCreator.habilitado()).thenReturn(true);
        when(processCreator.settings()).thenReturn(new SWorksProcessSettings(8, "19", "Cartão Consignado"));
        ArgumentCaptor<List<SWorksInputField>> campos = ArgumentCaptor.forClass(List.class);
        when(processCreator.create(campos.capture())).thenReturn(new SWorksCreateProcessResponse("proc-criado", 2001));
        when(sworks.execute(any())).thenReturn(sucesso());

        useCase.execute(request(null, "572313"));

        Map<String, String> porNome = campos.getValue().stream()
            .collect(java.util.stream.Collectors.toMap(SWorksInputField::nome, SWorksInputField::valor));
        assertEquals("000.000.000-00", porNome.get("cpf"), "cpf sai com máscara");
        assertEquals("11933989960", porNome.get("celular"), "celular sai sem máscara");
        assertEquals("01/01/1990", porNome.get("dtNascimento"), "data sai em dd/MM/yyyy");
        assertEquals("572313", porNome.get("Proposta"));
        assertEquals("19", porNome.get("cdProdut"));
        assertEquals("Cartão Consignado", porNome.get("tipoOperacao"));
        assertEquals("12", porNome.get("prazo"));
        assertEquals("5000", porNome.get("vlTotal"), "valor inteiro não leva .0");
        assertEquals("250", porNome.get("vlrParcela"), "vem de installmentAmount, não de chargedAmount");
        assertEquals("4800", porNome.get("vlrLiquido"));
    }

    @Test
    void omitePropostaQuandoOExternalIdNaoENumerico() {
        when(processCreator.habilitado()).thenReturn(true);
        ArgumentCaptor<List<SWorksInputField>> campos = ArgumentCaptor.forClass(List.class);
        when(processCreator.create(campos.capture())).thenReturn(new SWorksCreateProcessResponse("proc-criado", 2001));
        when(sworks.execute(any())).thenReturn(sucesso());

        useCase.execute(request(null, "PROP-9911"));

        assertFalse(campos.getValue().stream().anyMatch(campo -> campo.nome().equals("Proposta")));
    }

    @Test
    void enviaAoSworksQuandoOIdentificadorDoProcessoVemNoRequest() {
        ArgumentCaptor<SendPolicyDocumentCommand> comando = ArgumentCaptor.forClass(SendPolicyDocumentCommand.class);
        when(sworks.execute(comando.capture())).thenReturn(sucesso());

        IssuePolicyResponse resposta = useCase.execute(request("proc-1", "EXT-99"));

        assertEquals("SUCESSO", resposta.sworksStatus());
        assertEquals("guid-1", resposta.sworksGuidDocumento());
        assertEquals("proc-1", comando.getValue().identificadorProcesso());
        assertEquals(ticket, comando.getValue().ticket());
        assertEquals("EXT-99", comando.getValue().idProposal());
    }

    @Test
    void naoEnviaQuandoOIdentificadorNaoVem() {
        IssuePolicyResponse resposta = useCase.execute(request(null, "EXT-99"));

        assertNull(resposta.sworksStatus());
        assertEquals(termoAdesaoUrlEsperado, resposta.termoAdesaoUrl());
        verify(sworks, never()).execute(any());
    }

    @Test
    void naoEnviaQuandoOIdentificadorVemEmBranco() {
        useCase.execute(request("   ", "EXT-99"));

        verify(sworks, never()).execute(any());
    }

    @Test
    void naoTentaEnviarQuandoOTermoNaoPudoSerGerado() {
        when(termoAdesaoPdfService.generate(any(), any())).thenThrow(new IllegalStateException("template ausente"));

        IssuePolicyResponse resposta = useCase.execute(request("proc-1", "EXT-99"));

        assertNull(resposta.termoAdesaoUrl());
        assertNull(resposta.sworksStatus());
        verify(sworks, never()).execute(any());
    }

    @Test
    void falhaNoSworksNaoDerrubaAEmissaoEApareceNaResposta() {
        when(sworks.execute(any())).thenReturn(new DocumentDispatchResult.Failure(
            "EXT-99", "proc-1", "termo-adesao-" + ticket + ".pdf", "SWorksUnavailable", "SWorks indisponível (HTTP 500)"
        ));

        IssuePolicyResponse resposta = useCase.execute(request("proc-1", "EXT-99"));

        assertEquals(ticket, resposta.ticket());
        assertEquals(termoAdesaoUrlEsperado, resposta.termoAdesaoUrl());
        assertEquals("FALHA", resposta.sworksStatus());
        assertNull(resposta.sworksGuidDocumento());
    }

    @Test
    void excecaoInesperadaNoEnvioNaoDerrubaAEmissao() {
        when(sworks.execute(any())).thenThrow(new RuntimeException("bug qualquer"));

        IssuePolicyResponse resposta = useCase.execute(request("proc-1", "EXT-99"));

        assertEquals(ticket, resposta.ticket());
        assertEquals(termoAdesaoUrlEsperado, resposta.termoAdesaoUrl());
        assertNull(resposta.sworksStatus());
    }

    @Test
    void usaOTicketComoIdProposalQuandoNaoHaExternalId() {
        ArgumentCaptor<SendPolicyDocumentCommand> comando = ArgumentCaptor.forClass(SendPolicyDocumentCommand.class);
        when(sworks.execute(comando.capture())).thenReturn(sucesso());

        useCase.execute(request("proc-1", null));

        assertEquals(ticket, comando.getValue().idProposal());
    }

    private IssuePolicyRequest request(String identificador, String externalId) {
        return new IssuePolicyRequest(
            Convenio.CLT,
            false,
            5000.0,
            12,
            externalId,
            null,
            100.0,
            new IssuePolicyCliente(
                "Cliente de Teste",
                "00000000000",
                "1990-01-01",
                1,
                "M",
                "(11) 93398-9960",
                "cliente.teste@example.com",
                new IssuePolicyEndereco(
                    "01234567",
                    "Rua de Teste",
                    "100",
                    null,
                    "Centro",
                    "São Paulo",
                    "SP"
                )
            ),
            identificador,
            4800.0,
            250.0
        );
    }

    private DocumentDispatchResult.Success sucesso() {
        return new DocumentDispatchResult.Success(
            "EXT-99", "proc-1", "termo-adesao-" + ticket + ".pdf", "guid-1", new DocumentVerification.Confirmed("guid-1")
        );
    }

    private HeroSegurosCoverage cobertura() {
        return new HeroSegurosCoverage(1, "Morte", "5000", null);
    }

    private HeroSegurosQuotationResponse quotacao() {
        return new HeroSegurosQuotationResponse(
            true,
            List.of(new HeroSegurosQuotationPlan(
                7, "Prestamista", "Plano", "0", "100000", 18, 80, 12, "10,00", List.of(cobertura())
            ))
        );
    }

    private HeroSegurosPolicyResponse apolice() {
        return new HeroSegurosPolicyResponse(
            true,
            new HeroSegurosPolicyData(
                7, "Plano", true, 12, "0", "100000", 18, 80, 12,
                null, null, 0.0, 1000, "10,00", "5000",
                List.of(cobertura()),
                new HeroSegurosPolicy(ticket, "http://hero/policy")
            ),
            List.of()
        );
    }
}
