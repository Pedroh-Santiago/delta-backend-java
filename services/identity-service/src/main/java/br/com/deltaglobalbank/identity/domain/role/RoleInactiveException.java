package br.com.deltaglobalbank.identity.domain.role;

public final class RoleInactiveException extends RoleDomainException {

    public RoleInactiveException() {
        super("role_inactive");
    }
}
