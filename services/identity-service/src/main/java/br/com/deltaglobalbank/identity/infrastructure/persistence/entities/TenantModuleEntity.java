package br.com.deltaglobalbank.identity.infrastructure.persistence.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tenant_modules")
@SQLDelete(sql = "UPDATE identity.tenant_modules SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TenantModuleEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "enabled_at", nullable = false)
    private Instant enabledAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected TenantModuleEntity() {
    }

    public TenantModuleEntity(
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

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getEnabledAt() {
        return enabledAt;
    }

    public void setEnabledAt(Instant enabledAt) {
        this.enabledAt = enabledAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
