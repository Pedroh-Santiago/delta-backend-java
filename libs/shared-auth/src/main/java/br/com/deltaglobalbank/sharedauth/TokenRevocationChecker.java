package br.com.deltaglobalbank.sharedauth;

import java.time.Instant;
import java.util.UUID;

public interface TokenRevocationChecker {

    RevocationReason checkRevocation(UUID jti, UUID userId, Instant issuedAt);
}
