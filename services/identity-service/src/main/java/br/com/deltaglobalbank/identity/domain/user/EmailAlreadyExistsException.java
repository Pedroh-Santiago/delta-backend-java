package br.com.deltaglobalbank.identity.domain.user;

public final class EmailAlreadyExistsException extends UserDomainException {

    public EmailAlreadyExistsException() {
        super("email_already_exists");
    }
}
