package br.com.deltaglobalbank.identity.features.apiClients.listClients

import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ListClientsUseCase(private val jpaApiClientRepository: JpaApiClientRepository, private val tenantRepository: JpaTenantRepository, private val jpaApiClientRoleRepository : JpaApiClientRoleRepository, private val roleRepository: RoleRepository, private val jpaApiKeyRepository : JpaApiKeyRepository) {

    companion object { const val MAX_PAGE_SIZE = 100}

    @Transactional
    fun listClients(tenantId: UUID, page: Int, size: Int): ListClientsResponse {

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val pageable = PageRequest.of(safePage, safeSize, Sort.Direction.DESC, "createdAt")

        val clientsPage = jpaApiClientRepository.findByTenantId(tenantId, pageable)
        if (clientsPage.content.isEmpty()) return ListClientsResponse(emptyList(), safePage, safeSize, 0, 0)

        val clientIds = clientsPage.content.map { it.id }.toSet()
        val tenant = tenantRepository.findById(tenantId).orElse(null)

        val rolesByClientId = jpaApiClientRoleRepository.findAllByApiClientIdIn(clientIds)
            .groupBy { it.apiClientId }

        val roleIds = rolesByClientId.values.flatten().map { it.roleId }.toSet()
        val rolesById: Map<UUID, Role> =
            if (roleIds.isEmpty()) emptyMap() else roleRepository.findAllByIds(roleIds).associateBy { it.id }

        val now = Instant.now()

        val activeCountByClient = jpaApiKeyRepository
            .countActiveByApiClientIdIn(clientIds, now)
            .associate { it.apiClientId to it.total }

        val items = clientsPage.content.map { client ->
            val roleCodes = rolesByClientId[client.id].orEmpty()
                .mapNotNull { rolesById[it.roleId]?.code?.toString() }
            val activeKeys = (activeCountByClient[client.id] ?: 0L).toInt()

            ItemsResponse(
                id = client.id,
                tenantId = client.tenantId,
                tenantSlug = tenant?.slug ?: "unknown",
                name = client.name,
                description = client.description ?: "",
                status = client.status,
                roles = roleCodes,
                activeKeysCount = activeKeys,
                createdAt = client.createdAt
            )
        }

        return ListClientsResponse(
            items = items,
            page = safePage,
            size = safeSize,
            totalElements = clientsPage.totalElements,
            totalPages = clientsPage.totalPages,
        )
    }


}