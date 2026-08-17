package br.com.deltaglobalbank.identity.infrastructure.security.token;

public record GeneratedRefreshToken(
    String plainText,
    String hash
) {
}
