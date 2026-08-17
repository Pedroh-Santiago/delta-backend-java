package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public abstract sealed class ExchangeApiKeyException extends RuntimeException
    permits InvalidApiKeyException, ApiClientSuspendedForExchangeException,
        TenantInactiveForExchangeException, ApiKeyRequiredException {

    protected ExchangeApiKeyException(String message) {
        super(message);
    }
}
