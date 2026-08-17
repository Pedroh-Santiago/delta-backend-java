package br.com.deltaglobalbank.identity.domain.module;

import java.util.List;
import java.util.UUID;

public interface TenantModuleRepository {
    List<TenantModule> findAllByTenantId(UUID tenantId);

    List<TenantModule> findAllByTenantIdAndEnabled(UUID tenantId, boolean enabled);

    TenantModule findByTenantIdAndModuleId(UUID tenantId, UUID moduleId);

    TenantModule save(TenantModule tenantModule);
}
