package br.com.deltaglobalbank.identity.features.auth.refresh;

public record RefreshTokenContext(
    String ipAddress,
    String userAgent
) {
}
