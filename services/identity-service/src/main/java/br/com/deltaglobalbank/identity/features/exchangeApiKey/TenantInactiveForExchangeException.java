package br.com.deltaglobalbank.identity.features.exchangeApiKey;

public final class TenantInactiveForExchangeException extends ExchangeApiKeyException {
    public TenantInactiveForExchangeException() {
        super("tenant_inactive");
    }
}
