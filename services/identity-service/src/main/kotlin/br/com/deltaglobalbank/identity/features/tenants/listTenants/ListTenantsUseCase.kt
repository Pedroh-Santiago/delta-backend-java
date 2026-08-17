package br.com.deltaglobalbank.identity.features.tenants.listTenants

import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ListTenantsQuery(val page: Int, val size: Int)

@Service
class ListTenantsUseCase(
    private val tenantRepository: TenantRepository
) {
    companion object {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 20
    }

    @Transactional(readOnly = true)
    fun execute(query: ListTenantsQuery): ListTenantsResponse {
        val safePage = query.page.coerceAtLeast(0)
        val safeSize = query.size.coerceIn(1, MAX_PAGE_SIZE)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))

        val tenantsPage = tenantRepository.findPage(pageable)

        if (tenantsPage.content.isEmpty()) {
            return ListTenantsResponse(
                items = emptyList(),
                page = safePage,
                size = safeSize,
                totalElements = 0,
                totalPages = 0
            )
        }

        val items = tenantsPage.content.map { tenant ->
            val snapshot = tenant.snapshot()
            ListedTenant(
                id = snapshot.id,
                name = snapshot.name,
                slug = snapshot.slug,
                status = snapshot.status.toDatabaseValue(),
                createdAt = snapshot.createdAt
            )
        }

        return ListTenantsResponse(
            items = items,
            page = safePage,
            size = safeSize,
            totalElements = tenantsPage.totalElements,
            totalPages = tenantsPage.totalPages
        )
    }
}
