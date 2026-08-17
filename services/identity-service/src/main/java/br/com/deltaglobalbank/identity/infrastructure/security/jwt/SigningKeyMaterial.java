package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record SigningKeyMaterial(
    String kid,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey,
    String status
) {
}
