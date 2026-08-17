package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.time.Instant;
import java.util.UUID;

public record IssuedJwt(
    String token,
    UUID jti,
    Instant issuedAt,
    Instant expiresAt
) {
}
