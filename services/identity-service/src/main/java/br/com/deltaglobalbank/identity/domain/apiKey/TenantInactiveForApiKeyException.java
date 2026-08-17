package br.com.deltaglobalbank.identity.domain.apiKey;

public final class TenantInactiveForApiKeyException extends ApiKeyDomainException {

    public TenantInactiveForApiKeyException() {
        super("tenant_inactive");
    }
}
