package br.com.deltaglobalbank.identity.domain.role

import java.time.Instant
import java.util.UUID

class Role(
    val id: UUID,
    val code: RoleCode,
    val moduleId: UUID?,
    val description: String?,
    val createdAt: Instant
) {

    fun snapshot(): RoleSnapshot = RoleSnapshot(
        id = id,
        code = code,
        moduleId = moduleId,
        description = description,
        createdAt = createdAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Role) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Role(id=$id, code=$code)"
}

data class RoleSnapshot(
    val id: UUID,
    val code: RoleCode,
    val moduleId: UUID?,
    val description: String?,
    val createdAt: Instant
)
