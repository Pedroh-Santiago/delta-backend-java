package br.com.deltaglobalbank.products.infrastructure.persistence.repositories

import br.com.deltaglobalbank.products.domain.product.ProductType
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaProductRepository : JpaRepository<ProductEntity, UUID> {
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): ProductEntity?

    fun existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(
        tenantId: UUID, type: String, agreementName: String): Boolean

    @Query("""
    SELECT p FROM ProductEntity p
    WHERE p.tenantId = :tenantId
      AND p.type = :type
      AND (:agreementName IS NULL OR LOWER(p.agreementName) LIKE LOWER(CONCAT('%', CAST(:agreementName AS string), '%')))
      AND (:active IS NULL OR p.active = :active)
""")
    fun searchLending(
        @Param("tenantId") tenantId: UUID,
        @Param("type") type: String,
        @Param("agreementName") agreementName: String?,
        @Param("active") active: Boolean?,
        pageable: Pageable,
    ): Page<ProductEntity>

    @Query("""
    SELECT COUNT(p) > 0 FROM ProductEntity p
    WHERE p.tenantId = :tenantId
      AND p.type = :type
      AND p.active = true
      AND LOWER(p.agreementName) = LOWER(:agreementName)
      AND p.id <> :excludeId
""")
    fun existsAnotherActiveWithAgreement(
        @Param("tenantId") tenantId: UUID,
        @Param("type") type: String,
        @Param("agreementName") agreementName: String,
        @Param("excludeId") excludeId: UUID,
    ): Boolean

    @Query("""
    SELECT p FROM ProductEntity p
    WHERE p.tenantId = :tenantId
      AND (:type IS NULL OR p.type = :type)
      AND (:active IS NULL OR p.active = :active)
      AND (:agreementName IS NULL OR LOWER(p.agreementName) LIKE LOWER(CONCAT('%', CAST(:agreementName AS string), '%')))
    ORDER BY p.createdAt DESC
""")
    fun searchProducts(
        @Param("tenantId") tenantId: UUID,
        @Param("type") type: String?,
        @Param("active") active: Boolean?,
        @Param("agreementName") agreementName: String?,
    ): List<ProductEntity>

}