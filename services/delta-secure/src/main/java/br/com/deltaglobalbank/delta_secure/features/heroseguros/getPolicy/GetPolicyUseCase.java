package br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosGetPolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosGetPolicyResponse;
import feign.codec.DecodeException;
import org.springframework.stereotype.Service;

@Service
public class GetPolicyUseCase {

    private final HeroSegurosGetPolicyClient client;
    private final TokenService tokenService;

    public GetPolicyUseCase(HeroSegurosGetPolicyClient client, TokenService tokenService) {
        this.client = client;
        this.tokenService = tokenService;
    }

    public GetPolicyResponse execute(GetPolicyRequest request) {
        String authorization = "Bearer " + tokenService.getToken(request.convenio());

        HeroSegurosGetPolicyResponse response;
        try {
            response = client.getProposal(authorization, request.id());
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }

        return GetPolicyMappers.toGetPolicyResponse(response);
    }
}
