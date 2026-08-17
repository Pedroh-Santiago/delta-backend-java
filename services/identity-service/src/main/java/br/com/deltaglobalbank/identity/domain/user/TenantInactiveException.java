package br.com.deltaglobalbank.identity.domain.user;

public final class TenantInactiveException extends UserDomainException {

    public TenantInactiveException() {
        super("tenant_inactive");
    }
}
