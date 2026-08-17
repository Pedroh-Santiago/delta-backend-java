package br.com.deltaglobalbank.products.features.lending.listLendingProduct;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListedLendingProduct(
    UUID id,
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
    Instant updatedAt
) {
}
