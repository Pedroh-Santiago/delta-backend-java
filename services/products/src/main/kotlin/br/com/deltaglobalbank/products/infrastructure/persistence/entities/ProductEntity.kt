package br.com.deltaglobalbank.products.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    var id: UUID,

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "type", nullable = false)
    var type: String,

    @Column(name = "agreement_name", nullable = false)
    var agreementName: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "min_monthly_rate", nullable = false)
    var minMonthlyRate: BigDecimal,

    @Column(name = "max_monthly_rate", nullable = false)
    var maxMonthlyRate: BigDecimal,

    @Column(name = "min_months", nullable = false)
    var minMonths: Int,

    @Column(name = "max_months", nullable = false)
    var maxMonths: Int,

    @Column(name = "min_amount", nullable = false)
    var minAmount: BigDecimal,

    @Column(name = "max_amount", nullable = false)
    var maxAmount: BigDecimal,

    @Column(name = "commission_rate")
    var commissionRate: BigDecimal?,

    @Column(name = "active", nullable = false)
    var active: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by")
    var createdBy: UUID?,

    @Column(name = "updated_by")
    var updatedBy: UUID?,

)