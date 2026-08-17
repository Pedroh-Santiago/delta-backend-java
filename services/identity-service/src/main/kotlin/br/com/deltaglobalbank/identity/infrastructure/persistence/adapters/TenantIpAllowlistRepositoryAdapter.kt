package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantIpAllowlistRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TenantIpAllowlistRepositoryAdapter  (
    private val jpaTenantIpAllowlistRepository: JpaTenantIpAllowlistRepository
    ) : TenantIpAllowlistRepository {

    override fun findById(id: UUID): TenantIpAllowlist? =
        jpaTenantIpAllowlistRepository.findById(id).orElse(null)?.toDomain()

    override fun save(entry: TenantIpAllowlist): TenantIpAllowlist =
        jpaTenantIpAllowlistRepository.save(entry.toEntity()).toDomain()

    override fun findAllByTenantId(tenantId: UUID): List<TenantIpAllowlist> =
        jpaTenantIpAllowlistRepository.findAllByTenantId(tenantId).map { it.toDomain() }

    override fun existsByCidrAndTenantId(cidr: String, tenantId: UUID): Boolean =
        jpaTenantIpAllowlistRepository.existsByCidrAndTenantId(cidr, tenantId)

    override fun delete(id: UUID) =
        jpaTenantIpAllowlistRepository.deleteById(id)
}
