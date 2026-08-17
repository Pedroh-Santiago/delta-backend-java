package br.com.deltaglobalbank.identity.domain.module

import java.time.Instant
import java.util.UUID

class TenantModule(
    val id: UUID,
    val tenantId: UUID,
    val moduleId: UUID,
    enabled: Boolean,
    val enabledAt: Instant,
    updatedAt: Instant,
    val deletedAt: Instant?
) {

    companion object {
        fun create(id: UUID, tenantId: UUID, moduleId: UUID): TenantModule {
            val now = Instant.now()
            return TenantModule(
                id = id,
                tenantId = tenantId,
                moduleId = moduleId,
                enabled = true,
                enabledAt = now,
                updatedAt = now,
                deletedAt = null
            )
        }
    }

    private var _enabled: Boolean = enabled
    private var _updatedAt: Instant = updatedAt

    fun enable() {
        _enabled = true
        _updatedAt = Instant.now()
    }

    fun disable() {
        _enabled = false
        _updatedAt = Instant.now()
    }

    fun isEnabled(): Boolean = _enabled

    fun snapshot(): TenantModuleSnapshot = TenantModuleSnapshot(
        id = id,
        tenantId = tenantId,
        moduleId = moduleId,
        enabled = _enabled,
        enabledAt = enabledAt,
        updatedAt = _updatedAt,
        deletedAt = deletedAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TenantModule) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "TenantModule(id=$id, tenantId=$tenantId, moduleId=$moduleId, enabled=$_enabled)"
}

data class TenantModuleSnapshot(
    val id: UUID,
    val tenantId: UUID,
    val moduleId: UUID,
    val enabled: Boolean,
    val enabledAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
)
