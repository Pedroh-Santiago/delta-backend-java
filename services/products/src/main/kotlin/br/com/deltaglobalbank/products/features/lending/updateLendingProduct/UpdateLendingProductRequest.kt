package br.com.deltaglobalbank.products.features.lending.updateLendingProduct

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class UpdateLendingProductRequest(
    @field:NotBlank
    val agreementName: String,

    val displayName: String? = null,

    @field:NotNull @field:PositiveOrZero
    val minMonthlyRate: BigDecimal,

    @field:NotNull @field:PositiveOrZero
    val maxMonthlyRate: BigDecimal,

    @field:Positive
    val minMonths: Int,

    @field:Positive
    val maxMonths: Int,

    @field:NotNull @field:PositiveOrZero
    val minAmount: BigDecimal,

    @field:NotNull @field:PositiveOrZero
    val maxAmount: BigDecimal,

    @field:PositiveOrZero
    val commissionRate: BigDecimal? = null,

    val active: Boolean,
)