package br.com.deltaglobalbank.identity.domain.user;

public final class UserNotActiveException extends UserDomainException {

    public UserNotActiveException() {
        super("invalid_credentials");
    }
}
