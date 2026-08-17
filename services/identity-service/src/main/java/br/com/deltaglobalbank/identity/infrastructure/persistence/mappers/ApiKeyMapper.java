package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey.Snapshot;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;

public final class ApiKeyMapper {

    private ApiKeyMapper() {
    }

    public static ApiKey toDomain(ApiKeyEntity entity) {
        return ApiKey.restore(
            entity.getId(),
            entity.getApiClientId(),
            entity.getName(),
            new HashedPassword(entity.getKeyHash()),
            entity.getKeyPrefix(),
            entity.getFingerprint(),
            entity.getExpiresAt(),
            entity.getLastUsedAt(),
            entity.getRevokedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static ApiKeyEntity toEntity(ApiKey apiKey) {
        Snapshot snap = apiKey.snapshot();
        return new ApiKeyEntity(
            apiKey.getId(),
            apiKey.getApiClientId(),
            apiKey.getName(),
            apiKey.getKeyHash().value(),
            apiKey.getKeyPrefix(),
            apiKey.getFingerprint(),
            snap.expiresAt(),
            snap.lastUsedAt(),
            snap.revokedAt(),
            apiKey.getCreatedAt(),
            snap.updatedAt(),
            null
        );
    }

    public static void applyTo(ApiKey apiKey, ApiKeyEntity entity) {
        Snapshot snap = apiKey.snapshot();
        entity.setExpiresAt(snap.expiresAt());
        entity.setLastUsedAt(snap.lastUsedAt());
        entity.setRevokedAt(snap.revokedAt());
        entity.setUpdatedAt(snap.updatedAt());
    }
}
