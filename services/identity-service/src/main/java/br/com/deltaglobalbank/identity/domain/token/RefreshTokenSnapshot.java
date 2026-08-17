package br.com.deltaglobalbank.identity.domain.token;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenSnapshot(
    UUID id,
    UUID userId,
    String tokenHash,
    Instant expiresAt,
    Instant revokedAt,
    Instant createdAt,
    Instant lastUsedAt,
    String userAgent,
    String ipAddress
) {
}
