package br.com.deltaglobalbank.identity.features.auth.login;

public record LoginContext(
    String ipAddress,
    String userAgent
) {
}
