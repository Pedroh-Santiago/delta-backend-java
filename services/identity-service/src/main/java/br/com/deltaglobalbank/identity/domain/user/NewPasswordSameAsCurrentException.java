package br.com.deltaglobalbank.identity.domain.user;

public final class NewPasswordSameAsCurrentException extends UserDomainException {

    public NewPasswordSameAsCurrentException() {
        super("new_password_same_as_current");
    }
}
