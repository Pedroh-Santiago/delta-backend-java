package br.com.deltaglobalbank.identity.features.signingKeys

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyView

fun SigningKey.toView() = SigningKeyView(
    id = id, kid = kid, algorithm = algorithm,
    status = status().toDatabaseValue(),
    activatedAt = activatedAt,
    retiredAt = snapshot().retiredAt,
)