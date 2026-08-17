package br.com.deltaglobalbank.identity.domain.token;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class IssuedTokenAudit {

    private final UUID id;
    private final UUID jti;
    private final String principalType;
    private final UUID principalId;
    private final UUID tenantId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final String ipAddress;
    private final String userAgent;

    public IssuedTokenAudit(
        UUID id,
        UUID jti,
        String principalType,
        UUID principalId,
        UUID tenantId,
        Instant issuedAt,
        Instant expiresAt,
        String ipAddress,
        String userAgent
    ) {
        this.id = id;
        this.jti = jti;
        this.principalType = principalType;
        this.principalId = principalId;
        this.tenantId = tenantId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public UUID getId() {
        return id;
    }

    public UUID getJti() {
        return jti;
    }

    public String getPrincipalType() {
        return principalType;
    }

    public UUID getPrincipalId() {
        return principalId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public IssuedTokenAuditSnapshot snapshot() {
        return new IssuedTokenAuditSnapshot(
            id,
            jti,
            principalType,
            principalId,
            tenantId,
            issuedAt,
            expiresAt,
            ipAddress,
            userAgent
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof IssuedTokenAudit audit && id.equals(audit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "IssuedTokenAudit(id=" + id + ", jti=" + jti + ")";
    }
}
