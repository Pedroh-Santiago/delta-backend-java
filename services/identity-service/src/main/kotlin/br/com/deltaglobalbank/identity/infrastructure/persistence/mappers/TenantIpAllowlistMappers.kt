package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantIpAllowlistEntity

fun TenantIpAllowlistEntity.toDomain(): TenantIpAllowlist = TenantIpAllowlist(
    id = this.id,
    tenantId = this.tenantId,
    cidr = Cidr(this.cidr),
    description = this.description,
    createdAt = this.createdAt
)

fun TenantIpAllowlist.toEntity(): TenantIpAllowlistEntity{
    val s = snapshot()
    return TenantIpAllowlistEntity(
        id = s.id,
        tenantId = s.tenantId,
        cidr = s.cidr,
        description = s.description,
        createdAt = s.createdAt
    )
}

fun TenantIpAllowlist.applyTo(entity: TenantIpAllowlistEntity): TenantIpAllowlistEntity {
    val s = snapshot()
    entity.id = s.id
    entity.tenantId = s.tenantId
    entity.cidr = s.cidr
    entity.description = s.description
    entity.createdAt = s.createdAt
    return entity
}