package br.com.deltaglobalbank.products.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "agreement_name", nullable = false)
    private String agreementName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "min_monthly_rate", nullable = false)
    private BigDecimal minMonthlyRate;

    @Column(name = "max_monthly_rate", nullable = false)
    private BigDecimal maxMonthlyRate;

    @Column(name = "min_months", nullable = false)
    private int minMonths;

    @Column(name = "max_months", nullable = false)
    private int maxMonths;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected ProductEntity() {
    }

    public ProductEntity(
        UUID id,
        UUID tenantId,
        String type,
        String agreementName,
        String displayName,
        BigDecimal minMonthlyRate,
        BigDecimal maxMonthlyRate,
        int minMonths,
        int maxMonths,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal commissionRate,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.type = type;
        this.agreementName = agreementName;
        this.displayName = displayName;
        this.minMonthlyRate = minMonthlyRate;
        this.maxMonthlyRate = maxMonthlyRate;
        this.minMonths = minMonths;
        this.maxMonths = maxMonths;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.commissionRate = commissionRate;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getType() {
        return type;
    }

    public String getAgreementName() {
        return agreementName;
    }

    public void setAgreementName(String agreementName) {
        this.agreementName = agreementName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BigDecimal getMinMonthlyRate() {
        return minMonthlyRate;
    }

    public void setMinMonthlyRate(BigDecimal minMonthlyRate) {
        this.minMonthlyRate = minMonthlyRate;
    }

    public BigDecimal getMaxMonthlyRate() {
        return maxMonthlyRate;
    }

    public void setMaxMonthlyRate(BigDecimal maxMonthlyRate) {
        this.maxMonthlyRate = maxMonthlyRate;
    }

    public int getMinMonths() {
        return minMonths;
    }

    public void setMinMonths(int minMonths) {
        this.minMonths = minMonths;
    }

    public int getMaxMonths() {
        return maxMonths;
    }

    public void setMaxMonths(int maxMonths) {
        this.maxMonths = maxMonths;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
