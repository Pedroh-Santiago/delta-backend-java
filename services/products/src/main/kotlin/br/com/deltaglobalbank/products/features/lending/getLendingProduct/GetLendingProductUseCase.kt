package br.com.deltaglobalbank.products.features.lending.getLendingProduct

import br.com.deltaglobalbank.products.domain.product.ProductNotFound
import br.com.deltaglobalbank.products.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class GetLendingProductCommand(
    val productId: UUID,
    val tenantId: UUID
)

@Service
class GetLendingProductUseCase(
    private val productRepository: ProductRepository
){
    @Transactional(readOnly = true)
    fun execute(command: GetLendingProductCommand): GetLendingProductResponse {
        val product = productRepository.findById(command.productId, command.tenantId) ?: throw ProductNotFound()

        val s = product.snapshot()

        return GetLendingProductResponse(
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
}