package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.module.Module
import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity

fun ModuleEntity.toDomain(): Module = Module(
    id = this.id,
    code = ModuleCode(this.code),
    name = this.name,
    description = this.description,
    createdAt = this.createdAt
)

fun Module.toEntity(): ModuleEntity {
    val s = snapshot()
    return ModuleEntity(
        id = s.id,
        code = s.code.value,
        name = s.name,
        description = s.description,
        createdAt = s.createdAt
    )
}
