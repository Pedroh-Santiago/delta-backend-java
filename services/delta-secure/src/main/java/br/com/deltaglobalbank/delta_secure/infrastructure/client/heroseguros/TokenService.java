package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenResponse;
import feign.codec.DecodeException;
import org.springframework.stereotype.Component;

@Component
public class TokenService {

    private static final long MARGIN_SECONDS = 30L;

    private record CachedToken(String token, Instant expiresAt) {
    }

    private final HeroSegurosAuthClient authClient;
    private final ServiceAuthProperties props;
    private final Map<Convenio, CachedToken> cache = new HashMap<>();

    public TokenService(HeroSegurosAuthClient authClient, ServiceAuthProperties props) {
        this.authClient = authClient;
        this.props = props;
    }

    public synchronized String getToken(Convenio convenio) {
        CachedToken cached = cache.get(convenio);
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) {
            return cached.token();
        }

        ConvenioCredentials credentials = props.convenios().get(convenio.name().toLowerCase());
        if (credentials == null) {
            throw new IllegalArgumentException("Credenciais não configuradas para o convênio " + convenio);
        }

        HeroSegurosTokenResponse response;
        try {
            response = authClient.getToken(new HeroSegurosTokenRequest(
                credentials.grantType(),
                credentials.clientId(),
                credentials.clientSecret(),
                credentials.username(),
                credentials.password(),
                credentials.scope()
            ));
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }

        CachedToken newToken = new CachedToken(
            response.accessToken(),
            Instant.now().plusSeconds(response.expiresIn() - MARGIN_SECONDS)
        );
        cache.put(convenio, newToken);
        return newToken.token();
    }
}
