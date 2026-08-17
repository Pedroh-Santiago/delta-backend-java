package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity

fun SigningKeyEntity.toDomain(): SigningKey = SigningKey(
    id = this.id,
    kid = this.kid,
    algorithm = this.algorithm,
    publicKey = this.publicKey,
    privateKey = this.privateKey,
    status = SigningKeyStatus.fromDatabaseValue(this.status),
    createdAt = this.createdAt,
    activatedAt = this.activatedAt,
    retiredAt = this.retiredAt
)

fun SigningKey.toEntity(): SigningKeyEntity {
    val s = snapshot()
    return SigningKeyEntity(
        id = s.id,
        kid = s.kid,
        algorithm = s.algorithm,
        publicKey = s.publicKey,
        privateKey = s.privateKey,
        status = s.status.toDatabaseValue(),
        createdAt = s.createdAt,
        activatedAt = s.activatedAt,
        retiredAt = s.retiredAt
    )
}

fun SigningKey.applyTo(entity: SigningKeyEntity): SigningKeyEntity {
    val s = snapshot()
    entity.status = s.status.toDatabaseValue()
    entity.retiredAt = s.retiredAt
    return entity
}
