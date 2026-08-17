package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentRequest(
    UUID id,
    @NotBlank String type,
    @NotBlank String number,
    @NotBlank String issuer,
    @NotBlank String issuerState,
    @NotNull LocalDate issuedAt
) {
}
