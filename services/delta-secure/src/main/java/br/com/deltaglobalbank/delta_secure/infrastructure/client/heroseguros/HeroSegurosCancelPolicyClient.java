package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.cancelPolicy.HeroSegurosCancelPolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(
    name = "hero-seguros-cancel-policy",
    url = "${heroseguros.base-url}",
    configuration = HeroSegurosErrorDecoderConfig.class
)
public interface HeroSegurosCancelPolicyClient {
    @PostMapping(
        value = "/api/prestamista/proposal/cancel",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    HeroSegurosCancelPolicyResponse cancel(
        @RequestHeader("Authorization") String authorization,
        @RequestPart("doc_number") String docNumber,
        @RequestPart("ticket") String ticket,
        @RequestPart("reason") String reason
    );
}
