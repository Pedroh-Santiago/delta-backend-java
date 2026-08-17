package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public record ExchangeApiKeyResult(
    String internalToken,
    long expiresAtEpochSeconds,
    String principalId,
    String tenantId
) {
}
