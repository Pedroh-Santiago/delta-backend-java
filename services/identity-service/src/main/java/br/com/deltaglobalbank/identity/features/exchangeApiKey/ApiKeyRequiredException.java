package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public final class ApiKeyRequiredException extends ExchangeApiKeyException {
    public ApiKeyRequiredException() {
        super("api_key_required");
    }
}
