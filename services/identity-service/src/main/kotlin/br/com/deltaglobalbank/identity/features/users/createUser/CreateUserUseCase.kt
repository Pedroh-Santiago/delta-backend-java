package br.com.deltaglobalbank.identity.features.users.createUser

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.Email
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException
import br.com.deltaglobalbank.identity.domain.user.Password
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.domain.user.User.Companion.newUser
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import java.util.UUID
import java.time.Instant


data class CreateUserCommand(
    val tenantId: UUID,
    val fullName: String,
    val email: String,
    val roleCodes: List<RoleCode>,
    val grantedBy: UUID,
    val creatorRoles: List<String>
)


@Service
class CreateUserUseCase(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val roleRepository: RoleRepository,
    private val tenantModuleRepository: TenantModuleRepository,
    private val moduleRepository: ModuleRepository,
    private val userRoleRepository: JpaUserRoleRepository,
    private val passwordHasher: SpringPasswordHasher,
    private val temporaryPasswordGenerator: TemporaryPasswordGenerator
) {

    @Transactional
    fun execute(command: CreateUserCommand): CreateUserResponse {
        val tenant = tenantRepository.findById(command.tenantId) ?: throw TenantNotFoundException()

        if (!tenant.isActive()) throw TenantInactiveException()

        val email = Email(command.email)
        if (userRepository.existsByEmail(email)) throw EmailAlreadyExistsException()

        val roleCodesList = command.roleCodes.toMutableList()

        roleCodesList.replaceAll { RoleCode(it.toString().lowercase()) }

        if (roleCodesList.size != roleCodesList.toSet().size) throw DuplicateRoleException()

        val roles = roleCodesList.map { roleCode ->
            roleRepository.findByCode(roleCode) ?: throw RoleNotFoundException(roleCode.toString())
        }

        validateRolesAgainstTenantModules(roles, command.tenantId)
        validateRoleBelowPlatformAdmin(roles, command.creatorRoles)

        val temporaryPassword = temporaryPasswordGenerator.generatePassword()
        val hashedPassword = passwordHasher.hash(Password(temporaryPassword))

        val user = newUser(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = command.tenantId,
            fullName = command.fullName,
            email = email,
            hashedPassword
        )

        userRepository.save(user)

        val now = Instant.now()
        roles.forEach { role ->
            userRoleRepository.save(
                UserRoleEntity(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    userId = user.id,
                    roleId = role.id,
                    grantedAt = now,
                    grantedBy = command.grantedBy
                )
            )
        }

        return CreateUserResponse(
            user = CreatedUser(
                id = user.id,
                fullName = user.fullName,
                email = user.email.value,
                tenantId = user.tenantId,
                roles = roles.map { it.code },
                mustChangePassword = user.mustChangePassword(),
                createdAt = user.createdAt
            ),
            temporaryPassword = Password(temporaryPassword)
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