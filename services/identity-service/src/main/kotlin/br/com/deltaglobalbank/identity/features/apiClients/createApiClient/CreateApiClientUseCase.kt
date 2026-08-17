package br.com.deltaglobalbank.identity.features.apiClients.createApiClient

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient.Companion.newApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.collections.contains
import kotlin.collections.forEach


data class CreateApiClientCommand(
    val tenantId: UUID,
    val name: String,
    val description: String?,
    val roleCodes: List<RoleCode>,
    val creatorRoles: List<String>
)

@Service
class CreateApiClientUseCase(
    private val apiClientRepository: ApiClientRepository,
    private val tenantRepository: TenantRepository,
    private val roleRepository: RoleRepository,
    private val tenantModuleRepository: TenantModuleRepository,
    private val moduleRepository: ModuleRepository,
    private val apiClientRoleRepository: JpaApiClientRoleRepository
) {
    @Transactional
    fun execute(command: CreateApiClientCommand): CreateApiClientResponse {

        val tenant = tenantRepository.findById(command.tenantId) ?: throw TenantNotFoundException()
        if (!tenant.isActive()) throw TenantInactiveException()

        val roleCodesList = command.roleCodes.toMutableList()
        roleCodesList.replaceAll { RoleCode(it.toString().lowercase()) }
        if (roleCodesList.size != roleCodesList.toSet().size) throw DuplicateRoleException()

        val roles = roleCodesList.map { roleCode ->
            roleRepository.findByCode(roleCode) ?: throw RoleNotFoundException(roleCode.toString())
        }

        validateRolesAgainstTenantModules(roles, command.tenantId)
        validateRoleBelowPlatformAdmin(roles, command.creatorRoles)

        val apiClient = newApiClient(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = command.tenantId,
            name = command.name,
            description = command.description,
        )

        apiClientRepository.save(apiClient)

        val now = Instant.now()
        roles.forEach { role ->
            apiClientRoleRepository.save(
                ApiClientRoleEntity(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    apiClientId = apiClient.id,
                    roleId = role.id,
                    grantedAt = now
                )
            )
        }

        return CreateApiClientResponse(
            id = apiClient.id,
            tenantId = apiClient.tenantId,
            name = apiClient.name,
            description = apiClient.description,
            status = apiClient.snapshot().status.toDatabaseValue(),
            roles = roles.map { it.code },
            createdAt = apiClient.createdAt
        )

    }

    private fun validateRoleBelowPlatformAdmin(roles: List<Role>, creatorRoles: List<String>) {
        if (creatorRoles.contains("platform.admin")) return
        roles.forEach { role ->
            if (role.code.toString() == "platform.admin") throw AuthorizationDeniedException("denied")
        }
        return
    }


    private fun validateRolesAgainstTenantModules(roles: List<Role>, tenantId: UUID) {
        val rolesNeedingModule = roles.filter { it.moduleId != null }
        if (rolesNeedingModule.isEmpty()) return

        val enabledModules = tenantModuleRepository
            .findAllByTenantIdAndEnabled(tenantId, true)
        val enabledModuleIds = enabledModules.map { it.moduleId }.toSet()

        rolesNeedingModule.forEach { role ->
            if (role.moduleId !in enabledModuleIds) {
                val module = moduleRepository.findById(role.moduleId!!)
                throw ModuleNotEnabledForTenantException(module?.code?.toString() ?: "unknown")
            }
        }
    }
}