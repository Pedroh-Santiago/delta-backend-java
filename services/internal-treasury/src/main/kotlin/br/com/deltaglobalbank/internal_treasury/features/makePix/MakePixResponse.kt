package br.com.deltaglobalbank.internal_treasury.features.makePix

import java.time.Instant
import java.util.UUID

data class MakePixResponse (
    val id: UUID,
    val accountId: Long,
    val operationAmount: Long,
    val createdAt: Instant
)