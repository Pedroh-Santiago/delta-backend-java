package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    CustomerEntity findByIdAndTenantId(UUID id, UUID tenantId);
    CustomerEntity findByCpfAndTenantId(String cpf, UUID tenantId);
    boolean existsByCpfAndTenantId(String cpf, UUID tenantId);
    Page<CustomerEntity> findAllByTenantId(UUID tenantId, Pageable pageable);
    List<CustomerEntity> findAllByIdInAndTenantId(List<UUID> ids, UUID tenantId);

    @Query("""
    SELECT c FROM CustomerEntity c
    WHERE c.tenantId = :tenantId AND LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
""")
    List<CustomerEntity> searchByName(@Param("query") String query, @Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("""
    SELECT c FROM CustomerEntity c
    WHERE c.tenantId = :tenantId
    AND (:status IS NULL OR c.status = CAST(:status AS string))
    AND (:cpf IS NULL OR c.cpf LIKE CONCAT('%', CAST(:cpf AS string), '%'))
    AND (:fullName IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', CAST(:fullName AS string), '%')))
""")
    Page<CustomerEntity> findPageFiltered(
        @Param("tenantId") UUID tenantId,
        @Param("status") String status,
        @Param("cpf") String cpf,
        @Param("fullName") String fullName,
        Pageable pageable
    );
}
