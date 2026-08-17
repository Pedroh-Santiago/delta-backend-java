package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys

import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyView

data class ListSigningKeysResponse(
    val items: List<SigningKeyView>
)