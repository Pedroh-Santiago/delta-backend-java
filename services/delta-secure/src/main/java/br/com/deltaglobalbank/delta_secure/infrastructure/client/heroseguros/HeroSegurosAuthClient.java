package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "hero-seguros-auth",
    url = "${heroseguros.base-url}",
    configuration = HeroSegurosErrorDecoderConfig.class
)
public interface HeroSegurosAuthClient {
    @PostMapping("/oauth/token")
    HeroSegurosTokenResponse getToken(@RequestBody HeroSegurosTokenRequest request);
}
