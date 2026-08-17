package br.com.deltaglobalbank.identity.domain.token;

import java.time.Instant;
import java.util.UUID;

public record SigningKeySnapshot(
    UUID id,
    String kid,
    String algorithm,
    String publicKey,
    String privateKey,
    SigningKeyStatus status,
    Instant createdAt,
    Instant activatedAt,
    Instant retiredAt
) {
}
