package br.com.deltaglobalbank.identity.domain.user;

public final class TenantAccessDeniedException extends UserDomainException {

    public TenantAccessDeniedException() {
        super("tenant_access_denied");
    }
}
