package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import java.time.Clock;
import java.time.Instant;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAuthenticationFailed;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksNotConfigured;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksLoginResponse;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class SWorksTokenService {

    private static final long MARGIN_SECONDS = 30L;
    private static final long MIN_TTL_SECONDS = 5L;

    private record CachedToken(String token, Instant expiresAt) {
    }

    private final SWorksAuthClient authClient;
    private final SWorksProperties properties;
    private final Clock clock;

    private CachedToken cache;

    public SWorksTokenService(SWorksAuthClient authClient, SWorksProperties properties, Clock clock) {
        this.authClient = authClient;
        this.properties = properties;
        this.clock = clock;
    }

    public synchronized String getToken() {
        CachedToken cached = cache;
        if (cached != null && clock.instant().isBefore(cached.expiresAt())) {
            return cached.token();
        }
        return authenticate();
    }

    public synchronized void invalidate() {
        cache = null;
    }

    private String authenticate() {
        SWorksCredentials credentials = properties.auth();
        if (credentials.username().isBlank()) {
            throw new SWorksNotConfigured("SWORKS_USERNAME");
        }
        if (credentials.password().isBlank()) {
            throw new SWorksNotConfigured("SWORKS_PASSWORD");
        }

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", credentials.username());
        form.add("password", credentials.password());
        form.add("grant_type", credentials.grantType());

        SWorksLoginResponse response;
        try {
            response = authClient.login(form);
        } catch (DecodeException ex) {
            throw new SWorksInvalidResponse(ex);
        } catch (RetryableException ex) {
            throw new SWorksUnavailable(0, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName(), ex);
        } catch (FeignException ex) {
            throw new SWorksAuthenticationFailed(ex.status(), ex.contentUTF8(), ex);
        }
        long margemSegundos = Math.min(MARGIN_SECONDS, response.expiresIn() / 4);
        long ttlSeconds = Math.max(response.expiresIn() - margemSegundos, MIN_TTL_SECONDS);
        CachedToken fresh = new CachedToken(response.accessToken(), clock.instant().plusSeconds(ttlSeconds));
        cache = fresh;
        return fresh.token();
    }
}
