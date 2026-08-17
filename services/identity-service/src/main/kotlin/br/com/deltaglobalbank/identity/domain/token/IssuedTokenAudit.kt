package br.com.deltaglobalbank.identity.domain.token

import java.time.Instant
import java.util.UUID

class IssuedTokenAudit(
    val id: UUID,
    val jti: UUID,
    val principalType: String,
    val principalId: UUID,
    val tenantId: UUID,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val ipAddress: String?,
    val userAgent: String?
) {

    fun snapshot(): IssuedTokenAuditSnapshot = IssuedTokenAuditSnapshot(
        id = id,
        jti = jti,
        principalType = principalType,
        principalId = principalId,
        tenantId = tenantId,
        issuedAt = issuedAt,
        expiresAt = expiresAt,
        ipAddress = ipAddress,
        userAgent = userAgent
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IssuedTokenAudit) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "IssuedTokenAudit(id=$id, jti=$jti)"
}

data class IssuedTokenAuditSnapshot(
    val id: UUID,
    val jti: UUID,
    val principalType: String,
    val principalId: UUID,
    val tenantId: UUID,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val ipAddress: String?,
    val userAgent: String?
)
