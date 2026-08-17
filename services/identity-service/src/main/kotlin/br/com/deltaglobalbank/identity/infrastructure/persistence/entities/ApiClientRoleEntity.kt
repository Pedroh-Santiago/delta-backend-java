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
@Table(name = "api_client_roles")
@SQLDelete(sql = "UPDATE identity.api_client_roles SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class ApiClientRoleEntity(
    @Id
    var id: UUID,

    @Column(name = "api_client_id", nullable = false)
    var apiClientId: UUID,

    @Column(name = "role_id", nullable = false)
    var roleId: UUID,

    @Column(name = "granted_at", nullable = false)
    var grantedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)