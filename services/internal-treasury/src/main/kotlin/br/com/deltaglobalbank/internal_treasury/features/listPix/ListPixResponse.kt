package br.com.deltaglobalbank.internal_treasury.features.listPix

import java.time.Instant
import java.util.UUID

data class ListPixResponse (
    val content: List<MakePixItem>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
)

data class MakePixItem(
    val id: UUID,
    val accountId: Long,
    val recipientName: String,
    val recipientAccountNumber: String,
    val operationAmount: Long,
    val status: String,
    val createdAt: Instant
)