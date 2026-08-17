package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import java.util.function.Function;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksTokenExpired;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import feign.RetryableException;
import org.springframework.stereotype.Component;

@Component
public class SWorksBearerExecutor {

    private final SWorksTokenService tokenService;

    public SWorksBearerExecutor(SWorksTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public <T> T execute(Function<String, T> block) {
        try {
            return chamar(block);
        } catch (SWorksTokenExpired ex) {
            tokenService.invalidate();
            return chamar(block);
        }
    }

    private <T> T chamar(Function<String, T> block) {
        try {
            return block.apply(header(tokenService.getToken()));
        } catch (RetryableException ex) {
            throw new SWorksUnavailable(0, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName(), ex);
        }
    }

    private String header(String token) {
        return "Bearer " + token;
    }
}
