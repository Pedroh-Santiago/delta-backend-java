package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist.TenantIpAllowlistSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantIpAllowlistEntity;

public final class TenantIpAllowlistMapper {

    private TenantIpAllowlistMapper() {
    }

    public static TenantIpAllowlist toDomain(TenantIpAllowlistEntity entity) {
        return new TenantIpAllowlist(
            entity.getId(),
            entity.getTenantId(),
            new Cidr(entity.getCidr()),
            entity.getDescription(),
            entity.getCreatedAt()
        );
    }

    public static TenantIpAllowlistEntity toEntity(TenantIpAllowlist tenantIpAllowlist) {
        TenantIpAllowlistSnapshot s = tenantIpAllowlist.snapshot();
        return new TenantIpAllowlistEntity(
            s.id(),
            s.tenantId(),
            s.cidr(),
            s.description(),
            s.createdAt(),
            null
        );
    }

    public static TenantIpAllowlistEntity applyTo(TenantIpAllowlist tenantIpAllowlist, TenantIpAllowlistEntity entity) {
        TenantIpAllowlistSnapshot s = tenantIpAllowlist.snapshot();
        return new TenantIpAllowlistEntity(
            s.id(),
            s.tenantId(),
            s.cidr(),
            s.description(),
            s.createdAt(),
            entity.getDeletedAt()
        );
    }
}
