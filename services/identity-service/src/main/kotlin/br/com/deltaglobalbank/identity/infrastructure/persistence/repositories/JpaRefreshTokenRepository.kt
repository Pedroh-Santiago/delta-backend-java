package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaRefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?
    fun findAllByUserId(userId: UUID): List<RefreshTokenEntity>
    fun findAllByUserIdAndRevokedAtIsNull(userId: UUID): List<RefreshTokenEntity>
}
