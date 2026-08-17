package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.token.RefreshToken
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRefreshTokenRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RefreshTokenRepositoryAdapter(
    private val jpaRefreshTokenRepository: JpaRefreshTokenRepository
) : RefreshTokenRepository {

    override fun findById(id: UUID): RefreshToken? =
        jpaRefreshTokenRepository.findById(id).orElse(null)?.toDomain()

    override fun findByTokenHash(hash: String): RefreshToken? =
        jpaRefreshTokenRepository.findByTokenHash(hash)?.toDomain()

    override fun findAllByUserId(userId: UUID): List<RefreshToken> =
        jpaRefreshTokenRepository.findAllByUserId(userId).map { it.toDomain() }

    override fun findAllActiveByUserId(userId: UUID): List<RefreshToken> =
        jpaRefreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId).map { it.toDomain() }

    override fun save(refreshToken: RefreshToken): RefreshToken {
        val existing = jpaRefreshTokenRepository.findById(refreshToken.id).orElse(null)
        val entityToSave = if (existing != null) refreshToken.applyTo(existing) else refreshToken.toEntity()
        return jpaRefreshTokenRepository.save(entityToSave).toDomain()
    }

    override fun saveAll(refreshTokens: List<RefreshToken>): List<RefreshToken> {
        val entities = refreshTokens.map { token ->
            val existing = jpaRefreshTokenRepository.findById(token.id).orElse(null)
            if (existing != null) token.applyTo(existing) else token.toEntity()
        }
        return jpaRefreshTokenRepository.saveAll(entities).map { it.toDomain() }
    }
}
