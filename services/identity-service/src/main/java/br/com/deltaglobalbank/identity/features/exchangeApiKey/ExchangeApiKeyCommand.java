package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public record ExchangeApiKeyCommand(
    String apiKey,
    String sourceIp,
    String userAgent
) {
}
