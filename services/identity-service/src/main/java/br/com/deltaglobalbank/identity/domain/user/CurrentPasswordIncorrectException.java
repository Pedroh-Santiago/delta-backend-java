package br.com.deltaglobalbank.identity.domain.user;

public final class CurrentPasswordIncorrectException extends UserDomainException {

    public CurrentPasswordIncorrectException() {
        super("current_password_incorrect");
    }
}
