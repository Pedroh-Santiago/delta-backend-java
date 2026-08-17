package br.com.deltaglobalbank.identity.features.apiClients.createApiKey;

import java.time.Instant;
import java.util.UUID;

public record CreateApiKeyCommand(
    UUID apiClientId,
    UUID expectedTenantId,
    String name,
    Instant expiresAt
) {
}
