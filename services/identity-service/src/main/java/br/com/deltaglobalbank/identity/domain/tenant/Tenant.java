package br.com.deltaglobalbank.identity.domain.tenant;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Tenant {

    private final UUID id;
    private final String name;
    private final String slug;
    private final Instant createdAt;

    private TenantStatus status;
    private Instant updatedAt;

    public Tenant(
        UUID id,
        String name,
        String slug,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Tenant create(UUID id, String name, TenantSlug slug) {
        if (name.length() < 3 || name.length() > 255) {
            throw new IllegalArgumentException("invalid_tenant_name");
        }
        Instant now = Instant.now();
        return new Tenant(
            id,
            name,
            slug.value(),
            TenantStatus.ACTIVE,
            now,
            now
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public void activate() {
        status = TenantStatus.ACTIVE;
        updatedAt = Instant.now();
    }

    public void suspend() {
        status = TenantStatus.SUSPENDED;
        updatedAt = Instant.now();
    }

    public void deactivate() {
        status = TenantStatus.INACTIVE;
    }

    public TenantSnapshot snapshot() {
        return new TenantSnapshot(
            id,
            name,
            slug,
            status,
            createdAt,
            updatedAt
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Tenant tenant && id.equals(tenant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Tenant(id=" + id + ", slug=" + slug + ", status=" + status + ")";
    }
}
