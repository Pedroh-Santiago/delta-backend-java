package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public final class ApiClientSuspendedForExchangeException extends ExchangeApiKeyException {
    public ApiClientSuspendedForExchangeException() {
        super("api_client_suspended");
    }
}
