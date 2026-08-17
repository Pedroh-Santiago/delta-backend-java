package br.com.deltaglobalbank.identity.features.auth.login;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    boolean mustChangePassword
) {
    public LoginResponse(String accessToken, long expiresIn, boolean mustChangePassword) {
        this(accessToken, "Bearer", expiresIn, mustChangePassword);
    }
}
