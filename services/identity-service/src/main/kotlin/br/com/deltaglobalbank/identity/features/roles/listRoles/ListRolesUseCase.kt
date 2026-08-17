package br.com.deltaglobalbank.identity.features.roles.listRoles

import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ListRolesQuery(
    val includePlatformAdmin: Boolean
)

@Service
class ListRolesUseCase(
    private val roleRepository: RoleRepository
) {

    @Transactional(readOnly = true)
    fun execute(query: ListRolesQuery): ListRolesResponse {
        val items = roleRepository.findAll()
            .asSequence()
            .filter { query.includePlatformAdmin || it.code.value != PLATFORM_ADMIN }
            .sortedBy { it.code.value }
            .map { role ->
                val description = role.description
                ListedRole(
                    code = role.code.value,
                    name = description?.takeIf { it.isNotBlank() } ?: role.code.value,
                    description = description
                )
            }
            .toList()

        return ListRolesResponse(items = items)
    }

    companion object {
        private const val PLATFORM_ADMIN = "platform.admin"
    }
}
