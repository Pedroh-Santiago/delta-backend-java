package br.com.deltaglobalbank.products.infrastructure.persistence.adapters

import br.com.deltaglobalbank.products.domain.product.Product
import br.com.deltaglobalbank.products.domain.product.ProductRepository
import br.com.deltaglobalbank.products.domain.product.ProductType
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ProductRepositoryAdapter(private val jpa: JpaProductRepository) : ProductRepository {

    @Transactional
    override fun save(product: Product): Product {
        val existing = jpa.findById(product.id).orElse(null)
        val entity = if (existing != null) product.applyTo(existing) else product.toEntity()
        jpa.save(entity)
        return findById(product.id, product.tenantId)!!
    }

    override fun findById(id: UUID, tenantId: UUID): Product? =
        jpa.findByIdAndTenantId(id, tenantId)?.toDomain()

    override fun existsActiveByTypeAndAgreement(tenantId: UUID, type: ProductType, agreementName: String): Boolean =
        jpa.existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(tenantId, type.toDatabaseValue(), agreementName)

    override fun findLendingPage(tenantId: UUID, agreementName: String?, active: Boolean?, pageable: Pageable): Page<Product> =
        jpa.searchLending(tenantId, ProductType.LENDING.toDatabaseValue(), agreementName, active, pageable)
            .map { it.toDomain() }

    override fun existsAnotherActiveWithAgreement(tenantId: UUID, type: ProductType, agreementName: String, excludeId: UUID) =
        jpa.existsAnotherActiveWithAgreement(tenantId, type.toDatabaseValue(), agreementName, excludeId)

    override fun findProducts(tenantId: UUID, type: ProductType?, active: Boolean?, agreementName: String?): List<Product> =
        jpa.searchProducts(tenantId, type?.toDatabaseValue(), active, agreementName).map { it.toDomain() }
}