package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys;

public record RotateSigningKeyResponse(
    SigningKeyView newKey,
    SigningKeyView previousKey
) {
}
