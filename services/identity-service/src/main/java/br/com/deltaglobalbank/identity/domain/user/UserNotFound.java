package br.com.deltaglobalbank.identity.domain.user;

public final class UserNotFound extends UserDomainException {

    public UserNotFound() {
        super("User not found");
    }
}
