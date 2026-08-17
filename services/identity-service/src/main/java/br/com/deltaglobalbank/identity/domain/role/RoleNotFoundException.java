package br.com.deltaglobalbank.identity.domain.role;

public final class RoleNotFoundException extends RoleDomainException {

    public RoleNotFoundException() {
        super("role_not_found");
    }
}
