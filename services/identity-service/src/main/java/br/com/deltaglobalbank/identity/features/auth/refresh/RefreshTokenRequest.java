package br.com.deltaglobalbank.identity.features.auth.refresh;

public record RefreshTokenRequest(
    String refreshToken
) {
    public RefreshTokenRequest() {
        this(null);
    }
}
