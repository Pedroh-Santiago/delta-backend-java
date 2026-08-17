package br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey;

import java.util.UUID;

public record RevokeApiKeyCommand(
    UUID apiKeyId,
    UUID tenantId,
    UUID revokedBy
) {
}
