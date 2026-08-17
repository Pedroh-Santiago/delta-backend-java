package br.com.deltaglobalbank.identity.features.users.listUserRoles

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class ListUserRoleQuery(
    val tenantId: UUID,
    val userId: UUID
)

@Service
data class ListUserRoleUseCase(
    private val userRepository: UserRepository,
    private val userRoleRepository: JpaUserRoleRepository,
    private val roleRepository: RoleRepository,
    private val moduleRepository: ModuleRepository
){
    @Transactional(readOnly = true)
    fun execute(query: ListUserRoleQuery): ListUserRolesResponse{
        val user = userRepository.findById(query.userId) ?: throw UserNotFound()
        if (user.tenantId != query.tenantId) throw UserNotFound()

        val entries = userRoleRepository.findAllByUserId(user.id)
        if (entries.isEmpty()) return ListUserRolesResponse(items = emptyList())

        val roleIds = entries.map { it.roleId }.toSet()
        val rolesById = roleRepository.findAllByIds(roleIds).associateBy { it.id }

        val moduleIds = rolesById.values.mapNotNull { it.moduleId }.toSet()
        val moduleCodeById = moduleRepository.findAllByIds(moduleIds).associate { it.id to it.code.value }

        val items = entries.mapNotNull { entry ->
            val role = rolesById[entry.roleId] ?: return@mapNotNull null
            ListedUserRoles(
                roleCode = role.code.value,
                roleId = role.id,
                moduleCode = role.moduleId?.let { moduleCodeById[it] },
                grantedAt = entry.grantedAt,
                grantedBy = entry.grantedBy,
            )
        }
        return ListUserRolesResponse(items)
    }
}