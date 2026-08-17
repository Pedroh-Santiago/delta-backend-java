package br.com.deltaglobalbank.identity.domain.user;

public final class CannotRemoveOwnAdminRoleException extends UserDomainException {

    public CannotRemoveOwnAdminRoleException() {
        super("cannot_remove_own_admin_role");
    }
}
