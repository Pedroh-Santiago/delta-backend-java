package br.com.deltaglobalbank.identity.domain.apiKey;

public abstract sealed class ApiKeyDomainException extends RuntimeException
    permits ApiClientNotFoundException, ApiClientSuspendedException,
    TenantInactiveForApiKeyException, ApiKeyNotFoundException {

    protected ApiKeyDomainException(String message) {
        super(message);
    }
}
