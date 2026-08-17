package br.com.deltaglobalbank.sharedauth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ValidatedJwt(
    UUID jti,
    UUID subject,
    UUID tenantId,
    String principalType,
    List<String> roles,
    List<String> modules,
    boolean mustChangePassword,
    Instant expiresAt,
    Instant issuedAt
) {
}
