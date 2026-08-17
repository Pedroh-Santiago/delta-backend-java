package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RoleRepositoryAdapter(
    private val jpaRoleRepository: JpaRoleRepository,
    private val jpaUserRoleRepository: JpaUserRoleRepository
) : RoleRepository {

    override fun findById(id: UUID): Role? =
        jpaRoleRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCode(code: RoleCode): Role? =
        jpaRoleRepository.findByCode(code.value)?.toDomain()

    override fun findAll(): List<Role> =
        jpaRoleRepository.findAll().map { it.toDomain() }

    override fun findAllByIds(ids: Set<UUID>): List<Role> =
        jpaRoleRepository.findAllById(ids).map { it.toDomain() }

    override fun findAllByModuleId(moduleId: UUID): List<Role> =
        jpaRoleRepository.findAllByModuleId(moduleId).map { it.toDomain() }

    override fun findAllByUserId(userId: UUID): List<Role> {
        val roleIds = jpaUserRoleRepository.findAllByUserId(userId).map { it.roleId }.toSet()
        if (roleIds.isEmpty()) return emptyList()
        return jpaRoleRepository.findAllById(roleIds).map { it.toDomain() }
    }

    override fun findAllByApiClientId(apiClientId: UUID): List<Role> =
        jpaRoleRepository.findAllByApiClientId(apiClientId).map { it.toDomain() }
}
