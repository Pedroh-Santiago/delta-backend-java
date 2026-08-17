package br.com.deltaglobalbank.products.features.lending.listLendingProduct

import br.com.deltaglobalbank.products.domain.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class ListLendingProductsQuery(
    val tenantId: UUID,
    val page: Int,
    val size: Int?,
    val agreementName: String?,
    val active: Boolean?,
)

@Service
class ListLendingProductsUseCase(
    private val repository: ProductRepository
) {
    companion object {
        const val MAX = 100;
        const val DEFAULT = 20
    }

    @Transactional(readOnly = true)
    fun execute(query: ListLendingProductsQuery): ListLendingProductsResponse {
        val safePage = query.page.coerceAtLeast(0)
        val safeSize = (query.size ?: DEFAULT).coerceIn(1, MAX)

        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = repository.findLendingPage(query.tenantId, query.agreementName, query.active, pageable)

        val items = result.content.map { product ->
            val s = product.snapshot()
            ListedLendingProduct(
                id = s.id,
                type = s.type.toDatabaseValue(),
                agreementName = s.agreementName.value,
                displayName = s.displayName.value,
                minMonthlyRate = s.minMonthlyRate,
                maxMonthlyRate = s.maxMonthlyRate,
                minMonths = s.minMonths,
                maxMonths = s.maxMonths,
                minAmount = s.minAmount,
                maxAmount = s.maxAmount,
                commissionRate = s.commissionRate,
                active = s.active,
                createdAt = s.createdAt,
                updatedAt = s.updatedAt
            )
        }

        return ListLendingProductsResponse(
            items = items,
            page = safePage,
            size = safeSize,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }
}