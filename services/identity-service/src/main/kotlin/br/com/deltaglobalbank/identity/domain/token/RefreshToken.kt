package br.com.deltaglobalbank.identity.domain.token

import java.time.Instant
import java.util.UUID

class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    revokedAt: Instant?,
    val createdAt: Instant,
    lastUsedAt: Instant?,
    val userAgent: String?,
    val ipAddress: String?
) {
    companion object {
        fun newToken(
            id: UUID,
            userId: UUID,
            tokenHash: String,
            expiresAt: Instant,
            userAgent: String?,
            ipAddress: String?
        ): RefreshToken {
            return RefreshToken(
                id = id,
                userId = userId,
                tokenHash = tokenHash,
                expiresAt = expiresAt,
                revokedAt = null,
                createdAt = Instant.now(),
                lastUsedAt = null,
                userAgent = userAgent,
                ipAddress = ipAddress
            )
        }
    }

    private var _revokedAt: Instant? = revokedAt
    private var _lastUsedAt: Instant? = lastUsedAt

    fun revoke() {
        _revokedAt = Instant.now()
    }

    fun markUsed() {
        _lastUsedAt = Instant.now()
    }

    fun isActive(): Boolean = _revokedAt == null && !isExpired()

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun isRevoked(): Boolean = _revokedAt != null

    fun snapshot(): RefreshTokenSnapshot = RefreshTokenSnapshot(
        id = id,
        userId = userId,
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        revokedAt = _revokedAt,
        createdAt = createdAt,
        lastUsedAt = _lastUsedAt,
        userAgent = userAgent,
        ipAddress = ipAddress
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RefreshToken) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "RefreshToken(id=$id, userId=$userId)"
}

data class RefreshTokenSnapshot(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val createdAt: Instant,
    val lastUsedAt: Instant?,
    val userAgent: String?,
    val ipAddress: String?
)
