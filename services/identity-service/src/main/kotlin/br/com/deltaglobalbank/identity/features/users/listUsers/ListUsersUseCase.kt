package br.com.deltaglobalbank.identity.features.users.listUsers

import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


data class ListUsersQuery(
    val tenantIdFilter: UUID?,
    val page: Int,
    val size: Int,
    val requireTenantExists: Boolean = false
)

@Service
class ListUsersUseCase(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: JpaUserRoleRepository
) {
    companion object {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 20
    }

    @Transactional(readOnly = true)
    fun execute(query: ListUsersQuery): ListUsersResponse {
        if (query.requireTenantExists) {
            val tenantId = query.tenantIdFilter ?: throw TenantNotFoundException()
            tenantRepository.findById(tenantId) ?: throw TenantNotFoundException()
        }

        val safePage = query.page.coerceAtLeast(0)
        val safeSize = query.size.coerceIn(1, MAX_PAGE_SIZE)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val usersPage: Page<User> = if (query.tenantIdFilter != null) {
            userRepository.findPageByTenantId(query.tenantIdFilter, pageable)
        } else {
            userRepository.findPage(pageable)
        }

        if (usersPage.content.isEmpty()) {
            return ListUsersResponse(
                items = emptyList(),
                page = safePage,
                size = safeSize,
                totalElements = 0,
                totalPages = 0
            )
        }

        val tenantIds = usersPage.content.map { it.tenantId }.toSet()
        val tenantsBySlug = tenantIds.mapNotNull { tenantRepository.findById(it) }
            .associateBy { it.id }

        val userIds = usersPage.content.map { it.id }.toSet()
        val userRolesByUserId = userRoleRepository.findAllByUserIdIn(userIds)
            .groupBy { it.userId }

        val roleIds = userRolesByUserId.values.flatten().map { it.roleId }.toSet()
        val rolesById: Map<UUID, Role> = if (roleIds.isEmpty()) {
            emptyMap()
        } else {
            roleRepository.findAllByIds(roleIds).associateBy { it.id }
        }

        val items = usersPage.content.map { user ->
            val tenant = tenantsBySlug[user.tenantId]
            val roleCodes = userRolesByUserId[user.id].orEmpty()
                .mapNotNull { rolesById[it.roleId]?.code?.value }
            val snapshot = user.snapshot()
            ListedUser(
                id = user.id,
                fullName = user.fullName,
                email = user.email.value,
                tenantId = user.tenantId,
                tenantSlug = tenant?.slug ?: "unknown",
                status = snapshot.status.toDatabaseValue(),
                roles = roleCodes,
                mustChangePassword = snapshot.mustChangePassword,
                failedAttempts = snapshot.failedAttempts,
                lockedUntil = snapshot.lockedUntil,
                lastLoginAt = snapshot.lastLoginAt,
                createdAt = snapshot.createdAt,
            )
        }

        return ListUsersResponse(
            items = items,
            page = safePage,
            size = safeSize,
            totalElements = usersPage.totalElements,
            totalPages = usersPage.totalPages
        )
    }
}