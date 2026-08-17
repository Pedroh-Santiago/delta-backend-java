package br.com.deltaglobalbank.identity.domain.user;

public final class TenantNotActiveException extends UserDomainException {

    public TenantNotActiveException() {
        super("invalid_credentials");
    }
}
