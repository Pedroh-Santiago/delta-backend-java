package br.com.deltaglobalbank.internal_treasury.domain.idempotency

import java.util.UUID

interface IdempotencyRepository {
    fun save(key: IdempotencyKey): IdempotencyKey

    fun findByKey(key: UUID): IdempotencyKey?
}