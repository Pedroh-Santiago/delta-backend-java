package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TenantModuleRepositoryAdapter(
    private val jpaTenantModuleRepository: JpaTenantModuleRepository
) : TenantModuleRepository {

    override fun findAllByTenantId(tenantId: UUID): List<TenantModule> =
        jpaTenantModuleRepository.findAllByTenantId(tenantId).map { it.toDomain() }

    override fun findAllByTenantIdAndEnabled(tenantId: UUID, enabled: Boolean): List<TenantModule> =
        jpaTenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, enabled).map { it.toDomain() }

    override fun findByTenantIdAndModuleId(tenantId: UUID, moduleId: UUID): TenantModule? =
        jpaTenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId)?.toDomain()

    override fun save(tenantModule: TenantModule): TenantModule {
        val existing = jpaTenantModuleRepository.findById(tenantModule.id).orElse(null)
        val entityToSave = if (existing != null) tenantModule.applyTo(existing) else tenantModule.toEntity()
        return jpaTenantModuleRepository.save(entityToSave).toDomain()
    }
}
