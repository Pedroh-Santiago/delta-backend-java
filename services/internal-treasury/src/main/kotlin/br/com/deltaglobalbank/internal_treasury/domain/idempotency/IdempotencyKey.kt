package br.com.deltaglobalbank.internal_treasury.domain.idempotency

import java.time.Instant
import java.util.UUID

class IdempotencyKey (
    val key: UUID,
    val response: String,
    val createdAt: Instant
)