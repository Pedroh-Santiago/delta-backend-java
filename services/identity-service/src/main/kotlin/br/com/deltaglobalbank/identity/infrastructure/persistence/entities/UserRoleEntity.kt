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
@Table(name = "user_roles")
@SQLDelete(sql = "UPDATE identity.user_roles SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class UserRoleEntity(
    @Id
    var id: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "role_id", nullable = false)
    var roleId: UUID,

    @Column(name = "granted_at", nullable = false)
    var grantedAt: Instant = Instant.now(),

    @Column(name = "granted_by")
    var grantedBy: UUID? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)
