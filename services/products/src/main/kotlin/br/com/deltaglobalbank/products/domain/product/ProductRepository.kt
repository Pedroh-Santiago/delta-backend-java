package br.com.deltaglobalbank.products.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: UUID, tenantId: UUID): Product?
    fun existsActiveByTypeAndAgreement(tenantId: UUID, type: ProductType, agreementName: String): Boolean
    fun findLendingPage(tenantId: UUID, agreementName: String?, active: Boolean?, pageable: Pageable): Page<Product>
    fun existsAnotherActiveWithAgreement(tenantId: UUID, type: ProductType, agreementName: String, excludeId: UUID): Boolean
    fun findProducts(tenantId: UUID, type: ProductType?, active: Boolean?, agreementName: String?): List<Product>
}