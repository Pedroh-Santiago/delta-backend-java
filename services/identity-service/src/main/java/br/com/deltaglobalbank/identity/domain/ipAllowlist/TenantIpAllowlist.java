package br.com.deltaglobalbank.identity.domain.ipAllowlist;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TenantIpAllowlist {

    private final UUID id;
    private final UUID tenantId;
    private final Cidr cidr;
    private final String description;
    private final Instant createdAt;

    public TenantIpAllowlist(
        UUID id,
        UUID tenantId,
        Cidr cidr,
        String description,
        Instant createdAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.cidr = cidr;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static TenantIpAllowlist newTenantIpAllowlist(
        UUID id,
        UUID tenantId,
        Cidr cidr,
        String description
    ) {
        Instant now = Instant.now();
        return new TenantIpAllowlist(
            id,
            tenantId,
            cidr,
            description,
            now
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Cidr getCidr() {
        return cidr;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TenantIpAllowlist entry && entry.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public TenantIpAllowlistSnapshot snapshot() {
        return new TenantIpAllowlistSnapshot(
            id,
            tenantId,
            cidr.value(),
            description,
            createdAt
        );
    }

    public record TenantIpAllowlistSnapshot(
        UUID id,
        UUID tenantId,
        String cidr,
        String description,
        Instant createdAt
    ) {
    }
}
