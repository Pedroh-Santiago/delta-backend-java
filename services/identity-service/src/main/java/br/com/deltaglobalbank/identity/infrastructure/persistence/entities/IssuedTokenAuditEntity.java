package br.com.deltaglobalbank.identity.infrastructure.persistence.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issued_tokens_audit")
public class IssuedTokenAuditEntity {

    @Id
    private UUID id;

    @Column(name = "jti", nullable = false)
    private UUID jti;

    @Column(name = "principal_type", nullable = false)
    private String principalType;

    @Column(name = "principal_id", nullable = false)
    private UUID principalId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    protected IssuedTokenAuditEntity() {
    }

    public IssuedTokenAuditEntity(
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
}
