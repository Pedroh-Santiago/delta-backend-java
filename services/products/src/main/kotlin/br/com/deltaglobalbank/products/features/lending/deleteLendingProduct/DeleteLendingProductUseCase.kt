package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct

import br.com.deltaglobalbank.products.domain.product.ProductNotFound
import br.com.deltaglobalbank.products.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class DeleteLendingProductCommand(
    val productId: UUID,
    val tenantId: UUID,
    val deletedBy: UUID,
)

@Service
class DeleteLendingProductUseCase (
    private val repository: ProductRepository
){
    @Transactional
    fun execute(command: DeleteLendingProductCommand) {
        val product = repository.findById(command.productId, command.tenantId) ?: throw ProductNotFound()

        product.deactivate(command.deletedBy)
        repository.save(product)
    }
}