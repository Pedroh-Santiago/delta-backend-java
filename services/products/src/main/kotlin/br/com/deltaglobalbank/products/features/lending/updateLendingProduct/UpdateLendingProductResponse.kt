package br.com.deltaglobalbank.products.features.lending.updateLendingProduct

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class UpdateLendingProductResponse (
    val id: UUID,
    val tenantId: UUID,
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