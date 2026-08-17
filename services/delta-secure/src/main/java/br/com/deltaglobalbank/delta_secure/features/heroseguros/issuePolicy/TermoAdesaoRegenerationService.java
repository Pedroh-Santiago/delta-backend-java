package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.TermoAdesaoNaoEncontrado;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosGetPolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalData;
import feign.codec.DecodeException;
import org.springframework.stereotype.Service;

@Service
public class TermoAdesaoRegenerationService {

    private final TermoAdesaoReferenceRepository referenceRepository;
    private final HeroSegurosGetPolicyClient getPolicyClient;
    private final TokenService tokenService;
    private final TermoAdesaoPdfService termoAdesaoPdfService;

    public TermoAdesaoRegenerationService(
        TermoAdesaoReferenceRepository referenceRepository,
        HeroSegurosGetPolicyClient getPolicyClient,
        TokenService tokenService,
        TermoAdesaoPdfService termoAdesaoPdfService
    ) {
        this.referenceRepository = referenceRepository;
        this.getPolicyClient = getPolicyClient;
        this.tokenService = tokenService;
        this.termoAdesaoPdfService = termoAdesaoPdfService;
    }

    public byte[] regenerate(String ticket) {
        TermoAdesaoReference reference = referenceRepository.findByTicket(ticket);
        if (reference == null) {
            throw new TermoAdesaoNaoEncontrado(ticket);
        }

        String authorization = "Bearer " + tokenService.getToken(reference.convenio());
        HeroSegurosProposalData proposal;
        try {
            proposal = getPolicyClient.getProposal(authorization, reference.heroSegurosId()).data();
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }

        return termoAdesaoPdfService.generateFromProposal(proposal, ticket, reference.externalId());
    }
}
