package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosGetPolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "hero-seguros-get-policy",
    url = "${heroseguros.base-url}",
    configuration = HeroSegurosErrorDecoderConfig.class
)
public interface HeroSegurosGetPolicyClient {
    @GetMapping("/api/prestamista/proposal/{id}")
    HeroSegurosGetPolicyResponse getProposal(
        @RequestHeader("Authorization") String authorization,
        @PathVariable int id
    );
}
