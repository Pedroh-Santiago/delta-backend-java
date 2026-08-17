package br.com.deltaglobalbank.identity.domain.role;

public final class RoleInUseException extends RoleDomainException {

    public RoleInUseException() {
        super("role_in_use");
    }
}
