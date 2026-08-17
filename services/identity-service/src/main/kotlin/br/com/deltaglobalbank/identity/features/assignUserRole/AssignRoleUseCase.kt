package br.com.deltaglobalbank.identity.features.assignUserRole

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.TenantAccessDeniedException
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.collections.forEach

@Service
class AssignRoleUseCase(private val tenantModuleRepository : TenantModuleRepository,private val moduleRepository: ModuleRepository,private val userRepository : UserRepository,private val roleRepository : RoleRepository, private val jpaUserRoleRepository: JpaUserRoleRepository) {

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

    @Transactional
    fun assignRoleTenant( actingTenantId: UUID, principal : AuthenticatedPrincipal,  userId : UUID, request: AssignRolesRequest): AssignRolesResponse {
        val validUser = userRepository.findById(userId) ?: throw UserNotFound()

        if(validUser.tenantId != actingTenantId){
            throw UserNotFound()
        }

        val requestedRoles = request.rolesCodes.map { code ->
            roleRepository.findByCode(code) ?: throw RoleNotFoundException(code.toString())
        }

        validateRoleBelowPlatformAdmin(requestedRoles,	principal.roles)
        validateRolesAgainstTenantModules(requestedRoles, actingTenantId)

        val currentRoles = roleRepository.findAllByUserId(validUser.id)

        val rolesToAdd = requestedRoles subtract currentRoles.toSet()

        rolesToAdd.forEach { role ->
            jpaUserRoleRepository.save(
                UserRoleEntity(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    userId = validUser.id,
                    roleId = role.id,
                    grantedAt = Instant.now(),
                    grantedBy = principal.subject
                )
            )
        }

        val addRoles = rolesToAdd.map { it.code}
        val userCodeRoles = (currentRoles + rolesToAdd).map { it.code }

        val assignRolesResponse = AssignRolesResponse(
            userId = validUser.id,
            addRoles,
            userCodeRoles
        )

        return assignRolesResponse
    }
}