package br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosSearchPoliciesClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesResponse;
import feign.codec.DecodeException;
import org.springframework.stereotype.Service;

@Service
public class SearchPoliciesUseCase {

    private final HeroSegurosSearchPoliciesClient client;
    private final TokenService tokenService;

    public SearchPoliciesUseCase(HeroSegurosSearchPoliciesClient client, TokenService tokenService) {
        this.client = client;
        this.tokenService = tokenService;
    }

    public SearchPoliciesResponse execute(SearchPoliciesRequest request) {
        String authorization = "Bearer " + tokenService.getToken(request.convenio());
        HeroSegurosSearchPoliciesRequest heroSegurosRequest = SearchPoliciesMappers.toHeroSegurosSearchPoliciesRequest(request);

        HeroSegurosSearchPoliciesResponse heroSegurosResponse;
        try {
            heroSegurosResponse = client.search(authorization, heroSegurosRequest);
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }
        return SearchPoliciesMappers.toSearchPoliciesResponse(heroSegurosResponse);
    }
}
