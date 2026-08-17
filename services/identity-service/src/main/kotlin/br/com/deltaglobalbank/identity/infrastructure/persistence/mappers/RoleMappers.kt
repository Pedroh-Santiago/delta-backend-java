package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity

fun RoleEntity.toDomain(): Role = Role(
    id = this.id,
    code = RoleCode(this.code),
    moduleId = this.moduleId,
    description = this.description,
    createdAt = this.createdAt
)

fun Role.toEntity(): RoleEntity {
    val s = snapshot()
    return RoleEntity(
        id = s.id,
        code = s.code.value,
        moduleId = s.moduleId,
        description = s.description,
        createdAt = s.createdAt
    )
}
