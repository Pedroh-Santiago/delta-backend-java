package br.com.deltaglobalbank.identity.domain.role;

public final class RoleCodeAlreadyExistsException extends RoleDomainException {

    public RoleCodeAlreadyExistsException() {
        super("role_code_already_exists");
    }
}
