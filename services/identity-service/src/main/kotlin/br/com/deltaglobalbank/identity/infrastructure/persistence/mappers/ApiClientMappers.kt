package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientStatus
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity

fun ApiClientEntity.toDomain(): ApiClient = ApiClient(
    id = this.id,
    tenantId = this.tenantId,
    name = this.name,
    description = this.description,
    status = ApiClientStatus.fromDatabaseValue(this.status),
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun ApiClient.toEntity(): ApiClientEntity {
    val s = snapshot()
    return ApiClientEntity(
        id = s.id,
        tenantId = s.tenantId,
        name = s.name,
        description = s.description,
        status = s.status.toDatabaseValue(),
        createdAt = s.createdAt,
        updatedAt = s.updatedAt,
    )
}

fun ApiClient.applyTo(entity: ApiClientEntity): ApiClientEntity {
    val s = snapshot()
    entity.id = s.id
    entity.tenantId = s.tenantId
    entity.name = s.name
    entity.description = s.description
    entity.status = s.status.toDatabaseValue()
    entity.createdAt = s.createdAt
    entity.updatedAt = s.updatedAt
    return entity
}