package br.com.deltaglobalbank.identity.domain.user;

public final class DuplicateRoleException extends UserDomainException {

    public DuplicateRoleException() {
        super("duplicate_role");
    }
}
