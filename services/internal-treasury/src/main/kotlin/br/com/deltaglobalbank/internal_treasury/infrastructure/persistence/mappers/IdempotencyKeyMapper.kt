package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.IdempotencyKeyEntity

fun IdempotencyKeyEntity.toDomain(): IdempotencyKey = IdempotencyKey(
    key = this.key,
    response = this.response,
    createdAt = this.createdAt
)

fun IdempotencyKey.toEntity(): IdempotencyKeyEntity = IdempotencyKeyEntity(
    key = this.key,
    response = this.response,
    createdAt = this.createdAt,
)