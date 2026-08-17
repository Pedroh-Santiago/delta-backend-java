package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "hero-seguros-quotation",
    url = "${heroseguros.base-url}",
    configuration = HeroSegurosErrorDecoderConfig.class
)
public interface HeroSegurosQuotationClient {
    @PostMapping("/api/prestamista/quotation")
    HeroSegurosQuotationResponse quote(
        @RequestHeader("Authorization") String authorization,
        @RequestBody HeroSegurosQuotationRequest request
    );
}
