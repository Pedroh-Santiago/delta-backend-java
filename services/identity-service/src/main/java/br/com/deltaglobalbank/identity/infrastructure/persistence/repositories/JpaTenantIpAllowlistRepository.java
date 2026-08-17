package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantIpAllowlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTenantIpAllowlistRepository extends JpaRepository<TenantIpAllowlistEntity, UUID> {
    List<TenantIpAllowlistEntity> findAllByTenantId(UUID tenantId);

    boolean existsByCidrAndTenantId(String cidr, UUID tenantId);
}
