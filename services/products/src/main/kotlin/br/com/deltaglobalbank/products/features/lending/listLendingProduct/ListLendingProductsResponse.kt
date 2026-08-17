package br.com.deltaglobalbank.products.features.lending.listLendingProduct

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ListLendingProductsResponse(
    val items: List<ListedLendingProduct>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class ListedLendingProduct(
    val id: UUID,
    val type: String,
    val agreementName: String,
    val displayName: String,
    val minMonthlyRate: BigDecimal,
    val maxMonthlyRate: BigDecimal,
    val minMonths: Int,
    val maxMonths: Int,
    val minAmount: BigDecimal,
    val maxAmount: BigDecimal,
    val commissionRate: BigDecimal?,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)