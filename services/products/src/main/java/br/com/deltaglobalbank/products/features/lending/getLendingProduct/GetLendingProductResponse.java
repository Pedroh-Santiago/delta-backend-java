package br.com.deltaglobalbank.products.features.lending.getLendingProduct;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetLendingProductResponse(
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
