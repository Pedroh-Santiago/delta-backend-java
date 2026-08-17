package br.com.deltaglobalbank.products.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductSnapshot(
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
}
