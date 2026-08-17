package br.com.deltaglobalbank.products.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    ProductEntity findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(
        UUID tenantId, String type, String agreementName);

    @Query("""
    SELECT p FROM ProductEntity p
    WHERE p.tenantId = :tenantId
      AND p.type = :type
      AND (:agreementName IS NULL OR LOWER(p.agreementName) LIKE LOWER(CONCAT('%', CAST(:agreementName AS string), '%')))
      AND (:active IS NULL OR p.active = :active)
""")
    Page<ProductEntity> searchLending(
        @Param("tenantId") UUID tenantId,
        @Param("type") String type,
        @Param("agreementName") String agreementName,
        @Param("active") Boolean active,
        Pageable pageable
    );

    @Query("""
    SELECT COUNT(p) > 0 FROM ProductEntity p
    WHERE p.tenantId = :tenantId
      AND p.type = :type
      AND p.active = true
      AND LOWER(p.agreementName) = LOWER(:agreementName)
      AND p.id <> :excludeId
""")
    boolean existsAnotherActiveWithAgreement(
        @Param("tenantId") UUID tenantId,
        @Param("type") String type,
        @Param("agreementName") String agreementName,
        @Param("excludeId") UUID excludeId
    );

    @Query("""
    SELECT p FROM ProductEntity p
    WHERE p.tenantId = :tenantId
      AND (:type IS NULL OR p.type = :type)
      AND (:active IS NULL OR p.active = :active)
      AND (:agreementName IS NULL OR LOWER(p.agreementName) LIKE LOWER(CONCAT('%', CAST(:agreementName AS string), '%')))
    ORDER BY p.createdAt DESC
""")
    List<ProductEntity> searchProducts(
        @Param("tenantId") UUID tenantId,
        @Param("type") String type,
        @Param("active") Boolean active,
        @Param("agreementName") String agreementName
    );
}
