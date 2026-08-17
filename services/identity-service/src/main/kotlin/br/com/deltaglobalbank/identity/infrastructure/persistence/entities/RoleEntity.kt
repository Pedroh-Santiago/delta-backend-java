package br.com.deltaglobalbank.identity.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "roles")
class RoleEntity(
    @Id
    var id: UUID,

    @Column(name = "code", nullable = false)
    var code: String,

    @Column(name = "module_id")
    var moduleId: UUID? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
