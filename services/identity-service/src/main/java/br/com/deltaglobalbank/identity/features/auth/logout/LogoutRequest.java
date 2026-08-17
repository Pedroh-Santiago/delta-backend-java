package br.com.deltaglobalbank.identity.features.auth.logout;

public record LogoutRequest(
    String refreshToken
) {
    public LogoutRequest() {
        this(null);
    }
}
