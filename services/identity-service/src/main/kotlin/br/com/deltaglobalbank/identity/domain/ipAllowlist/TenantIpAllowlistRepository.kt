package br.com.deltaglobalbank.identity.domain.ipAllowlist

import java.util.UUID

interface TenantIpAllowlistRepository {
    fun save(entry: TenantIpAllowlist): TenantIpAllowlist
    fun findById(id: UUID): TenantIpAllowlist?
    fun findAllByTenantId(tenantId: UUID): List<TenantIpAllowlist>
    fun existsByCidrAndTenantId(cidr: String, tenantId: UUID): Boolean
    fun delete(id: UUID)
}