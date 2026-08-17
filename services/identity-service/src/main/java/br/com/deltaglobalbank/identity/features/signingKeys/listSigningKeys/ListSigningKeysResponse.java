package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys;

import java.util.List;

import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyView;

public record ListSigningKeysResponse(
    List<SigningKeyView> items
) {
}
