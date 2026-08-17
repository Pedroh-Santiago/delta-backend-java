package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.token.RefreshToken
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity

fun RefreshTokenEntity.toDomain(): RefreshToken = RefreshToken(
    id = this.id,
    userId = this.userId,
    tokenHash = this.tokenHash,
    expiresAt = this.expiresAt,
    revokedAt = this.revokedAt,
    createdAt = this.createdAt,
    lastUsedAt = this.lastUsedAt,
    userAgent = this.userAgent,
    ipAddress = this.ipAddress
)

fun RefreshToken.toEntity(): RefreshTokenEntity {
    val s = snapshot()
    return RefreshTokenEntity(
        id = s.id,
        userId = s.userId,
        tokenHash = s.tokenHash,
        expiresAt = s.expiresAt,
        revokedAt = s.revokedAt,
        createdAt = s.createdAt,
        lastUsedAt = s.lastUsedAt,
        userAgent = s.userAgent,
        ipAddress = s.ipAddress
    )
}

fun RefreshToken.applyTo(entity: RefreshTokenEntity): RefreshTokenEntity {
    val s = snapshot()
    entity.revokedAt = s.revokedAt
    entity.lastUsedAt = s.lastUsedAt
    return entity
}
