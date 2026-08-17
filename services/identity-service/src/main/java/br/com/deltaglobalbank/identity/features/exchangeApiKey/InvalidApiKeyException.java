package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public final class InvalidApiKeyException extends ExchangeApiKeyException {
    public InvalidApiKeyException() {
        super("invalid_api_key");
    }
}
