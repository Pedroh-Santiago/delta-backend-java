package br.com.deltaglobalbank.identity.domain.role;

public final class RoleProtectedException extends RoleDomainException {

    public RoleProtectedException() {
        super("role_protected");
    }
}
