package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaApiClientRepository : JpaRepository<ApiClientEntity, UUID> {
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<ApiClientEntity>
}