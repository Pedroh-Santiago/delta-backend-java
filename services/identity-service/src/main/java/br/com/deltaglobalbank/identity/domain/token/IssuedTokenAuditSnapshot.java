package br.com.deltaglobalbank.identity.domain.token;

import java.time.Instant;
import java.util.UUID;

public record IssuedTokenAuditSnapshot(
    UUID id,
    UUID jti,
    String principalType,
    UUID principalId,
    UUID tenantId,
    Instant issuedAt,
    Instant expiresAt,
    String ipAddress,
    String userAgent
) {
}
