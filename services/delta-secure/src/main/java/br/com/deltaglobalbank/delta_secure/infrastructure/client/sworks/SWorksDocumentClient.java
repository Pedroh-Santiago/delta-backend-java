package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksAttachDocumentRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksDocumentResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksProcessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "sworks-document",
    url = "${sworks.base-url}",
    configuration = SWorksErrorDecoderConfig.class
)
public interface SWorksDocumentClient {
    @PutMapping(
        value = "/api/v1/Processo/{identificador}/documentos",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void attachDocument(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("identificador") String identificador,
        @RequestBody SWorksAttachDocumentRequest request
    );

    @GetMapping("/api/v1/Processo/{identificador}")
    SWorksProcessResponse getProcess(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("identificador") String identificador
    );

    @GetMapping("/api/v1/Processo/{identificador}/documento/{identificadorDocumento}")
    SWorksDocumentResponse getDocument(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("identificador") String identificador,
        @PathVariable("identificadorDocumento") String identificadorDocumento
    );
}
