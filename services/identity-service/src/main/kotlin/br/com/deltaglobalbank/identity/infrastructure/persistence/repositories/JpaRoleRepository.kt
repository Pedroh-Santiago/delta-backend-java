package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaRoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByCode(code: String): RoleEntity?
    fun findAllByModuleId(moduleId: UUID): List<RoleEntity>

    @Query(
        """
    SELECT r FROM RoleEntity r
    JOIN ApiClientRoleEntity acr ON acr.roleId = r.id
    WHERE acr.apiClientId = :apiClientId
      AND acr.deletedAt IS NULL
"""
    )
    fun findAllByApiClientId(@Param("apiClientId") apiClientId: UUID): List<RoleEntity>
}
