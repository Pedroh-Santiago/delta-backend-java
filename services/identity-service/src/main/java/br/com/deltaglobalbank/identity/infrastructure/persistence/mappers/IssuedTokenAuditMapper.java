package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity;

public final class IssuedTokenAuditMapper {

    private IssuedTokenAuditMapper() {
    }

    public static IssuedTokenAudit toDomain(IssuedTokenAuditEntity entity) {
        return new IssuedTokenAudit(
            entity.getId(),
            entity.getJti(),
            entity.getPrincipalType(),
            entity.getPrincipalId(),
            entity.getTenantId(),
            entity.getIssuedAt(),
            entity.getExpiresAt(),
            entity.getIpAddress(),
            entity.getUserAgent()
        );
    }

    public static IssuedTokenAuditEntity toEntity(IssuedTokenAudit audit) {
        IssuedTokenAuditSnapshot s = audit.snapshot();
        return new IssuedTokenAuditEntity(
            s.id(),
            s.jti(),
            s.principalType(),
            s.principalId(),
            s.tenantId(),
            s.issuedAt(),
            s.expiresAt(),
            s.ipAddress(),
            s.userAgent()
        );
    }
}
