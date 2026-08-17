package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys;

import java.time.Instant;
import java.util.UUID;

public record SigningKeyView(
    UUID id,
    String kid,
    String algorithm,
    String status,
    Instant activatedAt,
    Instant retiredAt
) {
}
