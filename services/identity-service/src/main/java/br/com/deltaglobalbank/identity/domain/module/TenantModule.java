package br.com.deltaglobalbank.identity.domain.module;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TenantModule {

    private final UUID id;
    private final UUID tenantId;
    private final UUID moduleId;
    private final Instant enabledAt;
    private final Instant deletedAt;

    private boolean enabled;
    private Instant updatedAt;

    public TenantModule(
        UUID id,
        UUID tenantId,
        UUID moduleId,
        boolean enabled,
        Instant enabledAt,
        Instant updatedAt,
        Instant deletedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.moduleId = moduleId;
        this.enabled = enabled;
        this.enabledAt = enabledAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static TenantModule create(UUID id, UUID tenantId, UUID moduleId) {
        Instant now = Instant.now();
        return new TenantModule(
            id,
            tenantId,
            moduleId,
            true,
            now,
            now,
            null
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public Instant getEnabledAt() {
        return enabledAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void enable() {
        enabled = true;
        updatedAt = Instant.now();
    }

    public void disable() {
        enabled = false;
        updatedAt = Instant.now();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TenantModuleSnapshot snapshot() {
        return new TenantModuleSnapshot(
            id,
            tenantId,
            moduleId,
            enabled,
            enabledAt,
            updatedAt,
            deletedAt
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof TenantModule tenantModule && id.equals(tenantModule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TenantModule(id=" + id + ", tenantId=" + tenantId + ", moduleId=" + moduleId + ", enabled=" + enabled + ")";
    }
}
