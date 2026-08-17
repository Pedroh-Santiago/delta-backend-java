package br.com.deltaglobalbank.identity.domain.user;

public final class TenantNotFoundException extends UserDomainException {

    public TenantNotFoundException() {
        super("tenant_not_found");
    }
}
