package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity;

public final class TenantModuleMapper {

    private TenantModuleMapper() {
    }

    public static TenantModule toDomain(TenantModuleEntity entity) {
        return new TenantModule(
            entity.getId(),
            entity.getTenantId(),
            entity.getModuleId(),
            entity.isEnabled(),
            entity.getEnabledAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt()
        );
    }

    public static TenantModuleEntity toEntity(TenantModule tenantModule) {
        TenantModuleSnapshot s = tenantModule.snapshot();
        return new TenantModuleEntity(
            s.id(),
            s.tenantId(),
            s.moduleId(),
            s.enabled(),
            s.enabledAt(),
            s.updatedAt(),
            s.deletedAt()
        );
    }

    public static TenantModuleEntity applyTo(TenantModule tenantModule, TenantModuleEntity entity) {
        TenantModuleSnapshot s = tenantModule.snapshot();
        entity.setEnabled(s.enabled());
        entity.setUpdatedAt(s.updatedAt());
        return entity;
    }
}
