package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaTenantModuleRepository : JpaRepository<TenantModuleEntity, UUID> {
    fun findAllByTenantId(tenantId: UUID): List<TenantModuleEntity>
    fun findAllByTenantIdAndEnabled(tenantId: UUID, enabled: Boolean): List<TenantModuleEntity>
    fun findByTenantIdAndModuleId(tenantId: UUID, moduleId: UUID): TenantModuleEntity?
}
