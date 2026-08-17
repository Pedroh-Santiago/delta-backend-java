package br.com.deltaglobalbank.identity.domain.ipAllowlist;

import java.util.List;
import java.util.UUID;

public interface TenantIpAllowlistRepository {
    TenantIpAllowlist save(TenantIpAllowlist entry);

    TenantIpAllowlist findById(UUID id);

    List<TenantIpAllowlist> findAllByTenantId(UUID tenantId);

    boolean existsByCidrAndTenantId(String cidr, UUID tenantId);

    void delete(UUID id);
}
