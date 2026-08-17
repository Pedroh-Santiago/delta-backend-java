package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TenantRepositoryAdapter(
    private val jpaTenantRepository: JpaTenantRepository
) : TenantRepository {

    override fun findById(id: UUID): Tenant? =
        jpaTenantRepository.findById(id).orElse(null)?.toDomain()

    override fun findBySlug(slug: String): Tenant? =
        jpaTenantRepository.findBySlug(slug)?.toDomain()

    override fun save(tenant: Tenant): Tenant {
        val existing = jpaTenantRepository.findById(tenant.id).orElse(null)
        val entityToSave = if (existing != null) tenant.applyTo(existing) else tenant.toEntity()
        return jpaTenantRepository.save(entityToSave).toDomain()
    }

    override fun delete(id: UUID) {
        jpaTenantRepository.deleteById(id)
    }

    override fun existsBySlug(slug: String): Boolean = jpaTenantRepository.existsBySlug(slug)

    override fun findPage(pageable: Pageable): Page<Tenant> =
        jpaTenantRepository.findAll(pageable).map { it.toDomain() }
}
