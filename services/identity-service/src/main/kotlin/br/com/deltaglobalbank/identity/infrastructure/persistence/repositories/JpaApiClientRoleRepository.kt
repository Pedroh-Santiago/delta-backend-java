package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaApiClientRoleRepository : JpaRepository<ApiClientRoleEntity, UUID> {
    fun findAllByApiClientId(apiClientId: UUID): List<ApiClientRoleEntity>
    fun findByApiClientIdAndRoleId(apiClientId: UUID, roleId: UUID): ApiClientRoleEntity?

    fun findAllByApiClientIdIn(apiClientIds: Set<UUID>): List<ApiClientRoleEntity>

}