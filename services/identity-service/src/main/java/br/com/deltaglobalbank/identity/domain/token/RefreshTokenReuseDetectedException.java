package br.com.deltaglobalbank.identity.domain.token;

public final class RefreshTokenReuseDetectedException extends TokenDomainException {

    public RefreshTokenReuseDetectedException() {
        super("refresh_token_reuse_detected");
    }
}
