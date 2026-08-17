package br.com.deltaglobalbank.identity.domain.token;

public abstract sealed class TokenDomainException extends RuntimeException
    permits InvalidRefreshTokenException, RefreshTokenReuseDetectedException, MissingRefreshTokenException {

    protected TokenDomainException(String message) {
        super(message);
    }
}
