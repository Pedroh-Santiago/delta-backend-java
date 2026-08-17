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
@Table(name = "tenants")
@SQLDelete(sql = "UPDATE identity.tenants SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class TenantEntity(
    @Id
    var id: UUID,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "slug", nullable = false)
    var slug: String,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)
