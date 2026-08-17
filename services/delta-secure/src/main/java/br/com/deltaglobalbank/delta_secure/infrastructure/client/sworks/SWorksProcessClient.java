package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksCreateProcessRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksCreateProcessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "sworks-process",
    url = "${sworks.base-url}",
    configuration = SWorksErrorDecoderConfig.class
)
public interface SWorksProcessClient {
    @PostMapping(value = "/api/v1/Processo", consumes = MediaType.APPLICATION_JSON_VALUE)
    SWorksCreateProcessResponse createProcess(
        @RequestHeader("Authorization") String authorization,
        @RequestBody SWorksCreateProcessRequest request
    );

    @PostMapping("/api/v1/Processo/IniciarProcesso/{identificador}")
    void startProcess(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("identificador") String identificador
    );
}
