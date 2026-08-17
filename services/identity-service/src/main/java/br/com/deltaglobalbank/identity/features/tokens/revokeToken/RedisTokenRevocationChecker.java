package br.com.deltaglobalbank.identity.features.tokens.revokeToken;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.TokenRevocationStore;
import br.com.deltaglobalbank.sharedauth.RevocationReason;
import br.com.deltaglobalbank.sharedauth.TokenRevocationChecker;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenRevocationChecker implements TokenRevocationChecker {

    private final TokenRevocationStore store;

    public RedisTokenRevocationChecker(TokenRevocationStore store) {
        this.store = store;
    }

    @Override
    public RevocationReason checkRevocation(UUID jti, UUID userId, Instant issuedAt) {
        return store.checkRevocation(jti.toString(), userId, issuedAt);
    }
}
