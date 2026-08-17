package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun findAllByTenantId(tenantId: UUID): List<UserEntity>
    fun findAllByTenantId(tenantId: UUID, pagable: Pageable): Page<UserEntity>
}
