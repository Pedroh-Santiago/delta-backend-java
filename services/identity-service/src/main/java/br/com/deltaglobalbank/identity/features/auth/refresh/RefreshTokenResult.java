package br.com.deltaglobalbank.identity.features.auth.refresh;

public record RefreshTokenResult(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    boolean mustChangePassword
) {
    public RefreshTokenResult(String accessToken, String refreshToken, long expiresIn, boolean mustChangePassword) {
        this(accessToken, refreshToken, "Bearer", expiresIn, mustChangePassword);
    }
}
