package br.com.deltaglobalbank.delta_secure.features.heroseguros.cancelPolicy;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosCancelPolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.cancelPolicy.HeroSegurosCancelPolicyResponse;
import feign.codec.DecodeException;
import org.springframework.stereotype.Service;

@Service
public class CancelPolicyUseCase {

    private final HeroSegurosCancelPolicyClient client;
    private final TokenService tokenService;

    public CancelPolicyUseCase(HeroSegurosCancelPolicyClient client, TokenService tokenService) {
        this.client = client;
        this.tokenService = tokenService;
    }

    public CancelPolicyResponse execute(CancelPolicyRequest request) {
        String authorization = "Bearer " + tokenService.getToken(request.convenio());

        HeroSegurosCancelPolicyResponse heroSegurosResponse;
        try {
            heroSegurosResponse = client.cancel(
                authorization,
                request.docNumber(),
                request.ticket(),
                String.valueOf(request.reason())
            );
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }
        return CancelPolicyMappers.toCancelPolicyResponse(heroSegurosResponse);
    }
}
