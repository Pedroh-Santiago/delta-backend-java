package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantIpAllowlistEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaTenantIpAllowlistRepository : JpaRepository<TenantIpAllowlistEntity, UUID> {
    fun findAllByTenantId(tenantId: UUID): List<TenantIpAllowlistEntity>
    fun existsByCidrAndTenantId(cidr: String, tenantId: UUID): Boolean
}