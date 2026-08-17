package br.com.deltaglobalbank.identity.features.auth.login;

public record LoginResult(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    boolean mustChangePassword
) {
    public LoginResult(String accessToken, String refreshToken, long expiresIn, boolean mustChangePassword) {
        this(accessToken, refreshToken, "Bearer", expiresIn, mustChangePassword);
    }
}
