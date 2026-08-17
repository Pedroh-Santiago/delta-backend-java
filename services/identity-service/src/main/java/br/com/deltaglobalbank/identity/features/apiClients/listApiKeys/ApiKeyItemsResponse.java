package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyItemsResponse(
    UUID id,
    String name,
    String keyPrefix,
    Instant expiresAt,
    Instant revokedAt,
    Instant lastUsedAt,
    Instant createdAt
) {
}
