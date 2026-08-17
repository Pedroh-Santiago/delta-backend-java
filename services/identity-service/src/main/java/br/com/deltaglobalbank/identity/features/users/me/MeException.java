package br.com.deltaglobalbank.identity.features.users.me;

public abstract sealed class MeException extends RuntimeException
    permits PrincipalNotFoundException, UnsupportedPrincipalTypeException {

    protected MeException(String message) {
        super(message);
    }
}
