package br.com.deltaglobalbank.identity.domain.module

import java.time.Instant
import java.util.UUID

class Module(
    val id: UUID,
    val code: ModuleCode,
    val name: String,
    val description: String?,
    val createdAt: Instant
) {

    fun snapshot(): ModuleSnapshot = ModuleSnapshot(
        id = id,
        code = code,
        name = name,
        description = description,
        createdAt = createdAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Module) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Module(id=$id, code=$code)"
}

data class ModuleSnapshot(
    val id: UUID,
    val code: ModuleCode,
    val name: String,
    val description: String?,
    val createdAt: Instant
)
