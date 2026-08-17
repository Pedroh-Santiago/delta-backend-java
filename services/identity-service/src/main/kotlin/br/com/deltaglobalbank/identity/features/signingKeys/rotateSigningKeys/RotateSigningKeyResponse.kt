package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys

import java.time.Instant
import java.util.UUID

data class RotateSigningKeyResponse(
    val newKey: SigningKeyView,
    val previousKey: SigningKeyView?
)
data class SigningKeyView(
    val id: UUID,
    val kid: String,
    val algorithm: String,
    val status: String,
    val activatedAt: Instant?,
    val retiredAt: Instant?
)