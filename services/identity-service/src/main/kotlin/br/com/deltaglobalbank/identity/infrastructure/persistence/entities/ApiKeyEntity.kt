package br.com.deltaglobalbank.identity.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "api_keys", schema = "identity")
@SQLDelete(sql = "UPDATE identity.api_keys SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class ApiKeyEntity(
    @Id
    @Column(name = "id")
    var id: UUID,

    @Column(name = "api_client_id", nullable = false)
    var apiClientId: UUID,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "key_hash", nullable = false)
    var keyHash: String,

    @Column(name = "key_prefix", nullable = false)
    var keyPrefix: String,

    @Column(name = "fingerprint")
    var fingerprint: String,

    @Column(name = "expires_at")
    var expiresAt: Instant?,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant?,

    @Column(name = "revoked_at")
    var revokedAt: Instant?,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)