package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity

fun TenantModuleEntity.toDomain(): TenantModule = TenantModule(
    id = this.id,
    tenantId = this.tenantId,
    moduleId = this.moduleId,
    enabled = this.enabled,
    enabledAt = this.enabledAt,
    updatedAt = this.updatedAt,
    deletedAt = this.deletedAt
)

fun TenantModule.toEntity(): TenantModuleEntity {
    val s = snapshot()
    return TenantModuleEntity(
        id = s.id,
        tenantId = s.tenantId,
        moduleId = s.moduleId,
        enabled = s.enabled,
        enabledAt = s.enabledAt,
        updatedAt = s.updatedAt,
        deletedAt = s.deletedAt
    )
}

fun TenantModule.applyTo(entity: TenantModuleEntity): TenantModuleEntity {
    val s = snapshot()
    entity.enabled = s.enabled
    entity.updatedAt = s.updatedAt
    return entity
}
