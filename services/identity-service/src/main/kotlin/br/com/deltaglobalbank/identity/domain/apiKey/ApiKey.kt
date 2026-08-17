package br.com.deltaglobalbank.identity.domain.apiKey

import br.com.deltaglobalbank.identity.domain.user.HashedPassword
import java.time.Instant
import java.util.UUID

class ApiKey private constructor(
    val id: UUID,
    val apiClientId: UUID,
    val name: String,
    val keyHash: HashedPassword,
    val keyPrefix: String,
    val fingerprint: String,
    private var _expiresAt: Instant?,
    private var _lastUsedAt: Instant?,
    private var _revokedAt: Instant?,
    val createdAt: Instant,
    private var _updatedAt: Instant
) {

    fun isRevoked(): Boolean = _revokedAt != null
    fun isExpired(): Boolean = _expiresAt?.isBefore(Instant.now()) ?: false
    fun isActive(): Boolean = !isRevoked() && !isExpired()

    fun revoke() {
        if (isRevoked()) return  // idempotente
        val now = Instant.now()
        _revokedAt = now
        _updatedAt = now
    }

    fun markUsed() {
        val now = Instant.now()
        _lastUsedAt = now
        _updatedAt = now
    }

    fun snapshot(): Snapshot = Snapshot(
        expiresAt = _expiresAt,
        lastUsedAt = _lastUsedAt,
        revokedAt = _revokedAt,
        updatedAt = _updatedAt
    )

    data class Snapshot(
        val expiresAt: Instant?,
        val lastUsedAt: Instant?,
        val revokedAt: Instant?,
        val updatedAt: Instant
    )

    override fun equals(other: Any?): Boolean = other is ApiKey && other.id == this.id
    override fun hashCode(): Int = id.hashCode()

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 255

        fun create(
            id: UUID,
            apiClientId: UUID,
            name: String,
            keyHash: HashedPassword,
            keyPrefix: String,
            fingerprint: String,
            expiresAt: Instant?
        ): ApiKey {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "invalid_api_key_name_length"
            }
            require(keyPrefix.isNotBlank()) { "invalid_key_prefix" }
            require(fingerprint.length == 64) { "invalid_fingerprint" }
            expiresAt?.let {
                require(it.isAfter(Instant.now())) { "expires_at_in_past" }
            }

            val now = Instant.now()
            return ApiKey(
                id = id,
                apiClientId = apiClientId,
                name = name.trim(),
                keyHash = keyHash,
                keyPrefix = keyPrefix,
                fingerprint = fingerprint,
                _expiresAt = expiresAt,
                _lastUsedAt = null,
                _revokedAt = null,
                createdAt = now,
                _updatedAt = now
            )
        }

        fun restore(
            id: UUID,
            apiClientId: UUID,
            name: String,
            keyHash: HashedPassword,
            keyPrefix: String,
            fingerprint: String,
            expiresAt: Instant?,
            lastUsedAt: Instant?,
            revokedAt: Instant?,
            createdAt: Instant,
            updatedAt: Instant
        ): ApiKey = ApiKey(
            id = id,
            apiClientId = apiClientId,
            name = name,
            keyHash = keyHash,
            keyPrefix = keyPrefix,
            fingerprint = fingerprint,
            _expiresAt = expiresAt,
            _lastUsedAt = lastUsedAt,
            _revokedAt = revokedAt,
            createdAt = createdAt,
            _updatedAt = updatedAt
        )
    }
}