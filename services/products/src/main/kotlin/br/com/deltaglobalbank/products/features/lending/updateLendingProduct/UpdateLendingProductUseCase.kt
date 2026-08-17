package br.com.deltaglobalbank.products.features.lending.updateLendingProduct

import br.com.deltaglobalbank.products.domain.product.AgreementName
import br.com.deltaglobalbank.products.domain.product.DisplayName
import br.com.deltaglobalbank.products.domain.product.DuplicateActiveProduct
import br.com.deltaglobalbank.products.domain.product.ProductNotFound
import br.com.deltaglobalbank.products.domain.product.ProductRepository
import br.com.deltaglobalbank.products.domain.product.ProductType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class UpdateLendingProductCommand(
    val productId: UUID,
    val tenantId: UUID,
    val updatedBy: UUID,
    val request: UpdateLendingProductRequest
)

@Service
class UpdateLendingProductUseCase(
    private val productRepository: ProductRepository
){
    @Transactional
    fun execute(command: UpdateLendingProductCommand): UpdateLendingProductResponse {
        val req = command.request
        val product = productRepository.findById(command.productId, command.tenantId) ?: throw ProductNotFound()

        if (req.active && productRepository.existsAnotherActiveWithAgreement(
                command.tenantId,
                ProductType.LENDING,
                req.agreementName,
                product.id)
            ) throw DuplicateActiveProduct()

        product.updateProduct(
            agreementName = AgreementName(req.agreementName),
            displayName = req.displayName?.takeIf { it.isNotBlank() }?.let { DisplayName(it) }
                ?: DisplayName("Consignado ${req.agreementName}"),
            minMonthlyRate = req.minMonthlyRate,
            maxMonthlyRate = req.maxMonthlyRate,
            minMonths = req.minMonths,
            maxMonths = req.maxMonths,
            minAmount = req.minAmount,
            maxAmount = req.maxAmount,
            commissionRate = req.commissionRate,
            active = req.active, updatedBy = command.updatedBy,
        )

        val saved = productRepository.save(product)
        val s = saved.snapshot()
        return UpdateLendingProductResponse(
            id = s.id,
            tenantId = s.tenantId,
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
