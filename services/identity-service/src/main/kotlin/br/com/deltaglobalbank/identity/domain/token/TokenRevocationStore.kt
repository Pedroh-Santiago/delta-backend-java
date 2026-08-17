package br.com.deltaglobalbank.identity.domain.token

import br.com.deltaglobalbank.sharedauth.RevocationReason
import java.time.Duration
import java.time.Instant
import java.util.UUID

interface TokenRevocationStore {
    fun revokeJti(jti: String, ttl: Duration)
    fun revokeUser(userId: UUID)
    fun checkRevocation(jti: String, userId: UUID, issuedAt: Instant): RevocationReason?
}