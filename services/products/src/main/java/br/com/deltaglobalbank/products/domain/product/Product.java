package br.com.deltaglobalbank.products.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final UUID tenantId;
    private final ProductType type;
    private final Instant createdAt;
    private final UUID createdBy;

    private boolean active;
    private Instant updatedAt;
    private UUID updatedBy;
    private AgreementName agreementName;
    private DisplayName displayName;
    private BigDecimal minMonthlyRate;
    private BigDecimal maxMonthlyRate;
    private int minMonths;
    private int maxMonths;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal commissionRate;

    public Product(
        UUID id,
        UUID tenantId,
        ProductType type,
        AgreementName agreementName,
        DisplayName displayName,
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

    public boolean isActive() {
        return active;
    }

    public void deactivate(UUID updatedBy) {
        this.active = false;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public ProductSnapshot snapshot() {
        return new ProductSnapshot(
            id,
            tenantId,
            type,
            agreementName,
            displayName,
            minMonthlyRate,
            maxMonthlyRate,
            minMonths,
            maxMonths,
            minAmount,
            maxAmount,
            commissionRate,
            active,
            createdAt,
            updatedAt,
            createdBy,
            updatedBy
        );
    }

    public void updateProduct(
        AgreementName agreementName,
        DisplayName displayName,
        BigDecimal minMonthlyRate,
        BigDecimal maxMonthlyRate,
        int minMonths,
        int maxMonths,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal commissionRate,
        boolean active,
        UUID updatedBy
    ) {
        validateTerms(
            minMonthlyRate,
            maxMonthlyRate,
            minMonths,
            maxMonths,
            minAmount,
            maxAmount,
            commissionRate
        );
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
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Product product && product.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private static void validateTerms(
        BigDecimal minMonthlyRate,
        BigDecimal maxMonthlyRate,
        int minMonths,
        int maxMonths,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal commissionRate
    ) {
        require(minMonthlyRate.compareTo(BigDecimal.ZERO) >= 0, "min_monthly_rate_negative");
        require(maxMonthlyRate.compareTo(minMonthlyRate) >= 0, "max_monthly_rate_lt_min");
        require(minMonths >= 1, "min_months_lt_1");
        require(maxMonths >= minMonths, "max_months_lt_min");
        require(minAmount.compareTo(BigDecimal.ZERO) >= 0, "min_amount_negative");
        require(maxAmount.compareTo(minAmount) >= 0, "max_amount_lt_min");
        require(commissionRate == null || commissionRate.compareTo(BigDecimal.ZERO) >= 0, "commission_rate_negative");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static Product newProduct(
        UUID id,
        UUID tenantId,
        ProductType type,
        AgreementName agreementName,
        DisplayName displayName,
        BigDecimal minMonthlyRate,
        BigDecimal maxMonthlyRate,
        int minMonths,
        int maxMonths,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal commissionRate,
        UUID createdBy
    ) {
        validateTerms(
            minMonthlyRate,
            maxMonthlyRate,
            minMonths,
            maxMonths,
            minAmount,
            maxAmount,
            commissionRate
        );
        Instant now = Instant.now();
        return new Product(
            id,
            tenantId,
            type,
            agreementName,
            displayName,
            minMonthlyRate,
            maxMonthlyRate,
            minMonths,
            maxMonths,
            minAmount,
            maxAmount,
            commissionRate,
            true,
            now,
            now,
            createdBy,
            createdBy
        );
    }
}
