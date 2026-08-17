package br.com.deltaglobalbank.products.features.lending.createLendingProduct

import br.com.deltaglobalbank.products.domain.product.AgreementName
import br.com.deltaglobalbank.products.domain.product.DisplayName
import br.com.deltaglobalbank.products.domain.product.DuplicateActiveProduct
import br.com.deltaglobalbank.products.domain.product.Product
import br.com.deltaglobalbank.products.domain.product.ProductRepository
import br.com.deltaglobalbank.products.domain.product.ProductType
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class CreateLendingProductCommand(
    val tenantId: UUID,
    val createdBy: UUID,
    val request: CreateLendingProductRequest,
)

@Service
class CreateLendingProductUseCase(private val repository: ProductRepository) {
    @Transactional
    fun execute(command: CreateLendingProductCommand): CreateLendingProductResponse {
        val req = command.request
        val type = ProductType.LENDING

        if (repository.existsActiveByTypeAndAgreement(command.tenantId, type, req.agreementName))
            throw DuplicateActiveProduct()

        val displayName = req.displayName?.takeIf { it.isNotBlank() } ?: "Consignado ${req.agreementName}"

        val product = Product.newProduct(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = command.tenantId,
            type = type,
            agreementName = AgreementName(req.agreementName),
            displayName = DisplayName(displayName),
            minMonthlyRate = req.minMonthlyRate,
            maxMonthlyRate = req.maxMonthlyRate,
            minMonths = req.minMonths,
            maxAmount = req.maxAmount,
            minAmount = req.minAmount,
            maxMonths = req.maxMonths,
            commissionRate = req.commissionRate?.let { req.commissionRate },
            createdBy = command.createdBy,
        )

        val saved = repository.save(product)

        val s = saved.snapshot()
        return CreateLendingProductResponse(
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
        )
    }
}