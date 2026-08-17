package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksLoginResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sworks-auth", url = "${sworks.base-url}")
public interface SWorksAuthClient {
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    SWorksLoginResponse login(@RequestBody MultiValueMap<String, String> form);
}
