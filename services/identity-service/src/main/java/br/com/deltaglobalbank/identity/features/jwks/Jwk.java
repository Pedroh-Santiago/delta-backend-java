package br.com.deltaglobalbank.identity.features.jwks;

public record Jwk(
    String kty,
    String use,
    String alg,
    String kid,
    String n,
    String e
) {
}
