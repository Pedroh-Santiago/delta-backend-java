package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient.ApiClientSnapshot;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientStatus;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity;

public final class ApiClientMapper {

    private ApiClientMapper() {
    }

    public static ApiClient toDomain(ApiClientEntity entity) {
        return new ApiClient(
            entity.getId(),
            entity.getTenantId(),
            entity.getName(),
            entity.getDescription(),
            ApiClientStatus.fromDatabaseValue(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static ApiClientEntity toEntity(ApiClient apiClient) {
        ApiClientSnapshot s = apiClient.snapshot();
        return new ApiClientEntity(
            s.id(),
            s.tenantId(),
            s.name(),
            s.description(),
            s.status().toDatabaseValue(),
            s.createdAt(),
            s.updatedAt(),
            null
        );
    }

    public static ApiClientEntity applyTo(ApiClient apiClient, ApiClientEntity entity) {
        ApiClientSnapshot s = apiClient.snapshot();
        entity.setName(s.name());
        entity.setDescription(s.description());
        entity.setStatus(s.status().toDatabaseValue());
        entity.setUpdatedAt(s.updatedAt());
        return entity;
    }
}
