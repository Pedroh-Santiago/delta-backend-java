package br.com.deltaglobalbank.identity.domain.token;

public final class MissingRefreshTokenException extends TokenDomainException {

    public MissingRefreshTokenException() {
        super("missing_refresh_token");
    }
}
