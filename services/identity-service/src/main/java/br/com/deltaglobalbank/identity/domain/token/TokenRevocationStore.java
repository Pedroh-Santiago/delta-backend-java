package br.com.deltaglobalbank.identity.domain.token;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.RevocationReason;

public interface TokenRevocationStore {
    void revokeJti(String jti, Duration ttl);

    void revokeUser(UUID userId);

    RevocationReason checkRevocation(String jti, UUID userId, Instant issuedAt);
}
