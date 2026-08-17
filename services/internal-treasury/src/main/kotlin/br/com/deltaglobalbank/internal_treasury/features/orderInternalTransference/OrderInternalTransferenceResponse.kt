package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference

import java.time.Instant
import java.util.UUID

data class OrderInternalTransferenceResponse (
    val id: UUID,
    val payerId: Long,
    val accountNumber: Long,
    val amount: Int,
    val requestedAt: Instant
)