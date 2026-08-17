package br.com.deltaglobalbank.identity.domain.role;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Role {

    private final UUID id;
    private final RoleCode code;
    private final UUID moduleId;
    private final String description;
    private final String label;
    private final boolean active;
    private final Instant createdAt;

    public Role(
        UUID id,
        RoleCode code,
        UUID moduleId,
        String description,
        String label,
        boolean active,
        Instant createdAt
    ) {
        this.id = id;
        this.code = code;
        this.moduleId = moduleId;
        this.description = description;
        this.label = label;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Role activate() {
        return new Role(id, code, moduleId, description, label, true, createdAt);
    }

    public Role deactivate() {
        return new Role(id, code, moduleId, description, label, false, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public RoleCode getCode() {
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

    public RoleSnapshot snapshot() {
        return new RoleSnapshot(
            id,
            code,
            moduleId,
            description,
            label,
            active,
            createdAt
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Role role && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Role(id=" + id + ", code=" + code + ")";
    }
}
