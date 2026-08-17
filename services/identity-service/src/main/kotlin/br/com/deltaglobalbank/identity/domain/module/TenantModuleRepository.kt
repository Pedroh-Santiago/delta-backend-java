package br.com.deltaglobalbank.identity.domain.module

import java.util.UUID

interface TenantModuleRepository {
    fun findAllByTenantId(tenantId: UUID): List<TenantModule>
    fun findAllByTenantIdAndEnabled(tenantId: UUID, enabled: Boolean): List<TenantModule>
    fun findByTenantIdAndModuleId(tenantId: UUID, moduleId: UUID): TenantModule?
    fun save(tenantModule: TenantModule): TenantModule
}
