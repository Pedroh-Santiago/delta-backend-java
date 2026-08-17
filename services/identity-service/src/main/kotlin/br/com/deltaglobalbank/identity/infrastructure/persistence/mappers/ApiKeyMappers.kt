package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey
import br.com.deltaglobalbank.identity.domain.user.HashedPassword
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity

fun ApiKeyEntity.toDomain(): ApiKey = ApiKey.restore(
    id = id,
    apiClientId = apiClientId,
    name = name,
    keyHash = HashedPassword(keyHash),
    keyPrefix = keyPrefix,
    fingerprint = fingerprint,
    expiresAt = expiresAt,
    lastUsedAt = lastUsedAt,
    revokedAt = revokedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ApiKey.toEntity(): ApiKeyEntity {
    val snap = snapshot()
    return ApiKeyEntity(
        id = id,
        apiClientId = apiClientId,
        name = name,
        keyHash = keyHash.value,
        keyPrefix = keyPrefix,
        fingerprint = fingerprint,
        expiresAt = snap.expiresAt,
        lastUsedAt = snap.lastUsedAt,
        revokedAt = snap.revokedAt,
        createdAt = createdAt,
        updatedAt = snap.updatedAt
    )
}

fun ApiKey.applyTo(entity: ApiKeyEntity) {
    val snap = snapshot()
    entity.expiresAt = snap.expiresAt
    entity.lastUsedAt = snap.lastUsedAt
    entity.revokedAt = snap.revokedAt
    entity.updatedAt = snap.updatedAt
}