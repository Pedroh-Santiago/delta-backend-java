package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaUserRoleRepository : JpaRepository<UserRoleEntity, UUID> {
    fun findAllByUserId(userId: UUID): List<UserRoleEntity>
    fun findByUserIdAndRoleId(userId: UUID, roleId: UUID): UserRoleEntity?

    fun findAllByUserIdIn(userIds: Set<UUID>): List<UserRoleEntity>
}
