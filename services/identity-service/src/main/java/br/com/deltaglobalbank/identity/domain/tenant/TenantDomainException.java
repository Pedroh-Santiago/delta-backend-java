package br.com.deltaglobalbank.identity.domain.tenant;

public abstract sealed class TenantDomainException extends RuntimeException
    permits SlugAlreadyExistsException {

    protected TenantDomainException(String message) {
        super(message);
    }
}
