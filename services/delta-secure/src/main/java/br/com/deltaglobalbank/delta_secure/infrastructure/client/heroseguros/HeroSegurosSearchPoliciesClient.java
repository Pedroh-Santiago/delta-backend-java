package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "hero-seguros-search-policies",
    url = "${heroseguros.base-url}",
    configuration = HeroSegurosErrorDecoderConfig.class
)
public interface HeroSegurosSearchPoliciesClient {
    @PostMapping("/api/prestamista/proposals")
    HeroSegurosSearchPoliciesResponse search(
        @RequestHeader("Authorization") String authorization,
        @RequestBody HeroSegurosSearchPoliciesRequest request
    );
}
