package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTenantModuleRepository extends JpaRepository<TenantModuleEntity, UUID> {
    List<TenantModuleEntity> findAllByTenantId(UUID tenantId);

    List<TenantModuleEntity> findAllByTenantIdAndEnabled(UUID tenantId, boolean enabled);

    TenantModuleEntity findByTenantIdAndModuleId(UUID tenantId, UUID moduleId);
}
