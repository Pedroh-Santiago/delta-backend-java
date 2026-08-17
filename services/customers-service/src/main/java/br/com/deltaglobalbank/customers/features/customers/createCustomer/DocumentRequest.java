package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentRequest(
    @NotBlank String type,
    @NotBlank String number,
    @NotBlank String issuer,
    @NotBlank String issuerState,
    @NotNull LocalDate issuedAt
) {
}
