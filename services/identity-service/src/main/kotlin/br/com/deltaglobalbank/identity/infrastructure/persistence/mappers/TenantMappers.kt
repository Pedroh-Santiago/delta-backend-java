package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantStatus
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity

fun TenantEntity.toDomain(): Tenant = Tenant(
    id = this.id,
    name = this.name,
    slug = this.slug,
    status = TenantStatus.fromDatabaseValue(this.status),
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun Tenant.toEntity(): TenantEntity {
    val s = snapshot()
    return TenantEntity(
        id = s.id,
        name = s.name,
        slug = s.slug,
        status = s.status.toDatabaseValue(),
        createdAt = s.createdAt,
        updatedAt = s.updatedAt
    )
}

fun Tenant.applyTo(entity: TenantEntity): TenantEntity {
    val s = snapshot()
    entity.name = s.name
    entity.slug = s.slug
    entity.status = s.status.toDatabaseValue()
    entity.updatedAt = s.updatedAt
    return entity
}
