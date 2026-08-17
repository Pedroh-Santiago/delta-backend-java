package br.com.deltaglobalbank.identity.domain.user;

public final class InvalidCredentialsException extends UserDomainException {

    public InvalidCredentialsException() {
        super("invalid_credentials");
    }
}
