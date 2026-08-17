package br.com.deltaglobalbank.identity.features.users.me

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.features.users.me.MeResponse
import br.com.deltaglobalbank.identity.features.users.me.PrincipalNotFoundException
import br.com.deltaglobalbank.identity.features.users.me.UnsupportedPrincipalTypeException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class MeQuery(
    val principalId: UUID,
    val principalType: String
)

@Service
class MeUseCase(
    private val userRepository: UserRepository,
    private val apiClientRepository: ApiClientRepository,
    private val tenantRepository: TenantRepository,
    private val roleRepository: RoleRepository,
    private val tenantModuleRepository: TenantModuleRepository,
    private val moduleRepository: ModuleRepository
) {

    @Transactional(readOnly = true)
    fun execute(query: MeQuery): MeResponse {
        return when (query.principalType) {
            "user" -> resolveUser(query.principalId)
            "api_client" -> resolveApiClient(query.principalId)
            else -> throw UnsupportedPrincipalTypeException(query.principalType)
        }
    }

    private fun resolveUser(userId: UUID): MeResponse {
        val user = userRepository.findById(userId)
            ?: throw PrincipalNotFoundException()

        val tenant = tenantRepository.findById(user.tenantId)
            ?: throw PrincipalNotFoundException()

        val (roles, modules) = resolveRolesAndModulesForUser(user, tenant)
        val snapshot = user.snapshot()

        return MeResponse(
            id = user.id,
            principalType = "user",
            fullName = user.fullName,
            email = user.email.value,
            tenantId = tenant.id,
            tenantSlug = tenant.slug,
            tenantName = tenant.name,
            status = snapshot.status.toDatabaseValue(),
            roles = roles,
            modules = modules,
            mustChangePassword = user.mustChangePassword(),
            lastLoginAt = snapshot.lastLoginAt,
            createdAt = user.createdAt
        )
    }

    private fun resolveApiClient(apiClientId: UUID): MeResponse {
        val apiClient = apiClientRepository.findById(apiClientId)
            ?: throw PrincipalNotFoundException()

        val tenant = tenantRepository.findById(apiClient.tenantId)
            ?: throw PrincipalNotFoundException()

        val (roles, modules) = resolveRolesAndModulesForApiClient(apiClient, tenant)

        return MeResponse(
            id = apiClient.id,
            principalType = "api_client",
            name = apiClient.name,
            description = apiClient.description,
            tenantId = tenant.id,
            tenantSlug = tenant.slug,
            tenantName = tenant.name,
            status = apiClient.statusAsString(),
            roles = roles,
            modules = modules,
            createdAt = apiClient.createdAt
        )
    }

    private fun resolveRolesAndModulesForUser(
        user: User,
        tenant: Tenant
    ): Pair<List<String>, List<String>> {
        val enabledModuleIds = tenantModuleRepository
            .findAllByTenantIdAndEnabled(tenant.id, true)
            .map { it.moduleId }
            .toSet()

        val modules = if (enabledModuleIds.isEmpty()) {
            emptyList()
        } else {
            moduleRepository.findAllByIds(enabledModuleIds).map { it.code.value }
        }

        val roles = roleRepository.findAllByUserId(user.id)
            .filter { it.moduleId == null || it.moduleId in enabledModuleIds }
            .map { it.code.value }

        return roles to modules
    }

    private fun resolveRolesAndModulesForApiClient(
        apiClient: ApiClient,
        tenant: Tenant
    ): Pair<List<String>, List<String>> {
        val enabledModuleIds = tenantModuleRepository
            .findAllByTenantIdAndEnabled(tenant.id, true)
            .map { it.moduleId }
            .toSet()

        val modules = if (enabledModuleIds.isEmpty()) {
            emptyList()
        } else {
            moduleRepository.findAllByIds(enabledModuleIds).map { it.code.value }
        }

        val roles = roleRepository.findAllByApiClientId(apiClient.id)
            .filter { it.moduleId == null || it.moduleId in enabledModuleIds }
            .map { it.code.value }

        return roles to modules
    }
}