package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantSnapshot;
import br.com.deltaglobalbank.identity.domain.tenant.TenantStatus;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;

public final class TenantMapper {

    private TenantMapper() {
    }

    public static Tenant toDomain(TenantEntity entity) {
        return new Tenant(
            entity.getId(),
            entity.getName(),
            entity.getSlug(),
            TenantStatus.fromDatabaseValue(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static TenantEntity toEntity(Tenant tenant) {
        TenantSnapshot s = tenant.snapshot();
        return new TenantEntity(
            s.id(),
            s.name(),
            s.slug(),
            s.status().toDatabaseValue(),
            s.createdAt(),
            s.updatedAt(),
            null
        );
    }

    public static TenantEntity applyTo(Tenant tenant, TenantEntity entity) {
        TenantSnapshot s = tenant.snapshot();
        entity.setName(s.name());
        entity.setSlug(s.slug());
        entity.setStatus(s.status().toDatabaseValue());
        entity.setUpdatedAt(s.updatedAt());
        return entity;
    }
}
