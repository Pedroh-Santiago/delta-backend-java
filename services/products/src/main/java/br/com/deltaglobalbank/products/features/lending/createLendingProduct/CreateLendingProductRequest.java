package br.com.deltaglobalbank.products.features.lending.createLendingProduct;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateLendingProductRequest(
    @NotBlank String agreementName,
    String displayName,
    @NotNull @PositiveOrZero BigDecimal minMonthlyRate,
    @NotNull @PositiveOrZero BigDecimal maxMonthlyRate,
    @Positive int minMonths,
    @Positive int maxMonths,
    @NotNull @PositiveOrZero BigDecimal minAmount,
    @NotNull @PositiveOrZero BigDecimal maxAmount,
    BigDecimal commissionRate
) {
}
