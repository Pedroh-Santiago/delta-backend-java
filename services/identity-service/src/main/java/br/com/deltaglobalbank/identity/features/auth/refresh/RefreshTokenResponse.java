package br.com.deltaglobalbank.identity.features.auth.refresh;

public record RefreshTokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    boolean mustChangePassword
) {
    public RefreshTokenResponse(String accessToken, long expiresIn, boolean mustChangePassword) {
        this(accessToken, "Bearer", expiresIn, mustChangePassword);
    }
}
