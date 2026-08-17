package br.com.deltaglobalbank.identity.domain.role;

public abstract sealed class RoleDomainException extends RuntimeException
    permits RoleNotFoundException, RoleCodeAlreadyExistsException, RoleInUseException,
    RoleProtectedException, RoleInactiveException {

    protected RoleDomainException(String message) {
        super(message);
    }
}
