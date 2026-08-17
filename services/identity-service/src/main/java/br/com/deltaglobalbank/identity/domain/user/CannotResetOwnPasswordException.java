package br.com.deltaglobalbank.identity.domain.user;

public final class CannotResetOwnPasswordException extends UserDomainException {
    public CannotResetOwnPasswordException() {
        super("cannot_reset_own_password");
    }
}
