package br.com.deltaglobalbank.sharedauth

import java.time.Instant
import java.util.UUID

data class ValidatedJwt(
    val jti: UUID,
    val subject: UUID,
    val tenantId: UUID,
    val principalType: String,
    val roles: List<String>,
    val modules: List<String>,
    val mustChangePassword: Boolean,
    val expiresAt: Instant,
    val issuedAt: Instant
)
