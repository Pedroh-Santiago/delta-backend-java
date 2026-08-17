package br.com.deltaglobalbank.sharedauth

import java.time.Instant
import java.util.UUID

interface TokenRevocationChecker {

    fun checkRevocation(jti: UUID, userId: UUID, issuedAt: Instant): RevocationReason?
}