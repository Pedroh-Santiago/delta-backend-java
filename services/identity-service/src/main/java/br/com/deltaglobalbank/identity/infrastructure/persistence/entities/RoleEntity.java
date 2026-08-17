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
@Table(name = "roles")
@SQLDelete(sql = "UPDATE identity.roles SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RoleEntity {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "description")
    private String description;

    @Column(name = "label")
    private String label;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected RoleEntity() {
    }

    public RoleEntity(
        UUID id,
        String code,
        UUID moduleId,
        String description,
        String label,
        boolean active,
        Instant createdAt,
        Instant deletedAt
    ) {
        this.id = id;
        this.code = code;
        this.moduleId = moduleId;
        this.description = description;
        this.label = label;
        this.active = active;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
