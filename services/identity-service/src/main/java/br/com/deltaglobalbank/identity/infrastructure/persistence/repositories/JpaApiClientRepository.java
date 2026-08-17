package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaApiClientRepository extends JpaRepository<ApiClientEntity, UUID> {
    Page<ApiClientEntity> findByTenantId(UUID tenantId, Pageable pageable);
}
