package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference

import java.time.Instant
import java.util.UUID

data class ListInternalTransferenceResponse (
    val content: List<InternalTransferenceItem>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
)

data class InternalTransferenceItem (
    val id: UUID,
    val payerId: Long,
    val accountNumber: Long,
    val amount: Int,
    val status: String,
    val requestedAt: Instant
)