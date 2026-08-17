package br.com.deltaglobalbank.identity.features.apiClients.createApiKey;

import java.time.Instant;
import java.util.UUID;

public record CreatedApiKey(
    UUID id,
    UUID apiClientId,
    String name,
    String keyPrefix,
    Instant expiresAt,
    Instant createdAt
) {
}
