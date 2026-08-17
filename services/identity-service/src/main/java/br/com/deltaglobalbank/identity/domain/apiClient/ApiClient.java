package br.com.deltaglobalbank.identity.domain.apiClient;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ApiClient {

    private final UUID id;
    private final UUID tenantId;
    private final String name;
    private final String description;
    private final Instant createdAt;

    private ApiClientStatus status;
    private Instant updatedAt;

    public ApiClient(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        ApiClientStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApiClient newApiClient(
        UUID id,
        UUID tenantId,
        String name,
        String description
    ) {
        Instant now = Instant.now();
        return new ApiClient(
            id,
            tenantId,
            name,
            description,
            ApiClientStatus.ACTIVE,
            now,
            now
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
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

    @Override
    public boolean equals(Object other) {
        return other instanceof ApiClient apiClient && apiClient.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public ApiClientSnapshot snapshot() {
        return new ApiClientSnapshot(
            id,
            tenantId,
            name,
            description,
            status,
            createdAt,
            updatedAt
        );
    }

    public boolean isActive() {
        return status == ApiClientStatus.ACTIVE;
    }

    public String statusAsString() {
        return switch (status) {
            case ACTIVE -> "active";
            case SUSPENDED -> "suspended";
        };
    }

    public record ApiClientSnapshot(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        ApiClientStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
    }
}
