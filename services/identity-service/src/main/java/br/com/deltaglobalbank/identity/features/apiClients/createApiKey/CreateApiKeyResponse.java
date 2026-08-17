package br.com.deltaglobalbank.identity.features.apiClients.createApiKey;

public record CreateApiKeyResponse(
    CreatedApiKey apiKey,
    String key
) {
}
