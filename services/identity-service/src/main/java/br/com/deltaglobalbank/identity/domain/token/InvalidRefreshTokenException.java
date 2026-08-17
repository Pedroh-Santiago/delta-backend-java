package br.com.deltaglobalbank.identity.domain.token;

public final class InvalidRefreshTokenException extends TokenDomainException {

    public InvalidRefreshTokenException() {
        super("invalid_refresh_token");
    }
}
