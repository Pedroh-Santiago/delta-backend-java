package br.com.deltaglobalbank.identity.features.users.removeUserRole

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.user.CannotRemoveOwnAdminRoleException
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class RemoveUserRoleCommand(
    val tenantId: UUID,
    val userId: UUID,
    val roleCode: RoleCode,
    val principal: AuthenticatedPrincipal
)

@Service
class RemoveUserRoleUseCase(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: JpaUserRoleRepository
){
    @Transactional
    fun execute(command: RemoveUserRoleCommand){

        val user = userRepository.findById(command.userId) ?: throw UserNotFound()
        if (user.tenantId != command.tenantId) throw UserNotFound()

        val adminRoles = setOf("identity.admin", "platform.admin")
        if (command.principal.subject == command.userId &&
            command.roleCode.value in adminRoles) {
            throw CannotRemoveOwnAdminRoleException()
        }

        val role = roleRepository.findByCode(command.roleCode)
            ?: throw RoleNotFoundException(command.roleCode.value)

        val entry = userRoleRepository.findByUserIdAndRoleId(user.id, role.id) ?: return

        userRoleRepository.delete(entry)
    }
}