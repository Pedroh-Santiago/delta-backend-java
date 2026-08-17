package br.com.deltaglobalbank.identity.features.jwks;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksController {

    private final JwksUseCase jwksUseCase;

    public JwksController(JwksUseCase jwksUseCase) {
        this.jwksUseCase = jwksUseCase;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwkResponse> jwks() {
        JwkResponse response = jwksUseCase.execute();

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
            .body(response);
    }
}
