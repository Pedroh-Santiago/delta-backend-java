package br.com.deltaglobalbank.products.domain.product

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class Product(
    val id: UUID,
    val tenantId: UUID,
    val type: ProductType,
    agreementName: AgreementName,
    displayName: DisplayName,
    minMonthlyRate: BigDecimal,
    maxMonthlyRate: BigDecimal,
    minMonths: Int,
    maxMonths: Int,
    minAmount: BigDecimal,
    maxAmount: BigDecimal,
    commissionRate: BigDecimal?,
    active: Boolean,
    val createdAt: Instant,
    updatedAt: Instant,
    val createdBy: UUID?,
    updatedBy: UUID?,
) {
    private var _active = active
    private var _updatedAt = updatedAt
    private var _updatedBy = updatedBy
    private var _agreementName = agreementName
    private var _displayName = displayName
    private var _minMonthlyRate = minMonthlyRate
    private var _maxMonthlyRate = maxMonthlyRate
    private var _minMonths = minMonths
    private var _maxMonths = maxMonths
    private var _minAmount = minAmount
    private var _maxAmount = maxAmount
    private var _commissionRate = commissionRate

    val active: Boolean get() = _active

    fun deactivate(updatedBy: UUID?) {
        _active = false
        _updatedAt = Instant.now()
        _updatedBy = updatedBy
    }

    override fun equals(other: Any?) = other is Product && other.id == id

    override fun hashCode() = id.hashCode()

    fun snapshot(): ProductSnapshot = ProductSnapshot(
        id = id,
        tenantId = tenantId,
        type = type,
        agreementName = _agreementName,
        displayName = _displayName,
        minMonthlyRate = _minMonthlyRate,
        maxMonthlyRate = _maxMonthlyRate,
        minMonths = _minMonths,
        maxAmount = _maxAmount,
        minAmount = _minAmount,
        maxMonths = _maxMonths,
        commissionRate = _commissionRate,
        active = active,
        createdAt = createdAt,
        updatedAt = _updatedAt,
        createdBy = createdBy,
        updatedBy = _updatedBy
    )

    fun updateProduct(
        agreementName: AgreementName,
        displayName: DisplayName,
        minMonthlyRate: BigDecimal,
        maxMonthlyRate: BigDecimal,
        minMonths: Int,
        maxMonths: Int,
        minAmount: BigDecimal,
        maxAmount: BigDecimal,
        commissionRate: BigDecimal?,
        active: Boolean,
        updatedBy: UUID?
    ){
        validateTerms(
            minMonthlyRate,
            maxMonthlyRate,
            minMonths,
            maxMonths,
            minAmount,
            maxAmount,
            commissionRate
        )
        _agreementName = agreementName
        _displayName = displayName
        _minMonthlyRate = minMonthlyRate
        _maxMonthlyRate = maxMonthlyRate
        _minMonths = minMonths
        _maxMonths = maxMonths
        _minAmount = minAmount
        _maxAmount = maxAmount
        _commissionRate = commissionRate
        _active = active
        _updatedAt = Instant.now()
        _updatedBy = updatedBy
    }

    companion object {
        private fun validateTerms(
            minMonthlyRate: BigDecimal,
            maxMonthlyRate: BigDecimal,
            minMonths: Int,
            maxMonths: Int,
            minAmount: BigDecimal,
            maxAmount: BigDecimal,
            commissionRate: BigDecimal?
        ){
            require(minMonthlyRate >= BigDecimal.ZERO) { "min_monthly_rate_negative" }
            require(maxMonthlyRate >= minMonthlyRate) { "max_monthly_rate_lt_min" }
            require(minMonths >= 1) { "min_months_lt_1" }
            require(maxMonths >= minMonths) { "max_months_lt_min" }
            require(minAmount >= BigDecimal.ZERO) { "min_amount_negative" }
            require(maxAmount >= minAmount) { "max_amount_lt_min" }
            require(commissionRate == null || commissionRate >= BigDecimal.ZERO) { "commission_rate_negative" }
        }

        fun newProduct(
            id: UUID,
            tenantId: UUID,
            type: ProductType,
            agreementName: AgreementName,
            displayName: DisplayName,
            minMonthlyRate: BigDecimal,
            maxMonthlyRate: BigDecimal,
            minMonths: Int,
            maxMonths: Int,
            minAmount: BigDecimal,
            maxAmount: BigDecimal,
            commissionRate: BigDecimal?,
            createdBy: UUID?,
        ): Product {
            validateTerms(
                minMonthlyRate,
                maxMonthlyRate,
                minMonths,
                maxMonths,
                minAmount,
                maxAmount,
                commissionRate
            )
            val now = Instant.now()
            return Product(
                id = id,
                tenantId = tenantId,
                type = type,
                agreementName = agreementName,
                displayName = displayName,
                minMonthlyRate = minMonthlyRate,
                maxMonthlyRate = maxMonthlyRate,
                minMonths = minMonths,
                maxMonths = maxMonths,
                minAmount = minAmount,
                maxAmount = maxAmount,
                commissionRate = commissionRate,
                active = true,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                updatedBy = createdBy,
            )
        }
    }
}

data class ProductSnapshot(
    val id: UUID,
    val tenantId: UUID,
    val type: ProductType,
    val agreementName: AgreementName,
    val displayName: DisplayName,
    val minMonthlyRate: BigDecimal,
    val maxMonthlyRate: BigDecimal,
    val minMonths: Int,
    val maxMonths: Int,
    val minAmount: BigDecimal,
    val maxAmount: BigDecimal,
    val commissionRate: BigDecimal?,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: UUID?,
    val updatedBy: UUID?
)