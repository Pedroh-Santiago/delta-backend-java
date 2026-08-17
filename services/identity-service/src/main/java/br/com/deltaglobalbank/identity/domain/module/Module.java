package br.com.deltaglobalbank.identity.domain.module;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Module {

    private final UUID id;
    private final ModuleCode code;
    private final String name;
    private final String description;
    private final Instant createdAt;

    public Module(
        UUID id,
        ModuleCode code,
        String name,
        String description,
        Instant createdAt
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ModuleCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ModuleSnapshot snapshot() {
        return new ModuleSnapshot(
            id,
            code,
            name,
            description,
            createdAt
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Module module && id.equals(module.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Module(id=" + id + ", code=" + code + ")";
    }
}
