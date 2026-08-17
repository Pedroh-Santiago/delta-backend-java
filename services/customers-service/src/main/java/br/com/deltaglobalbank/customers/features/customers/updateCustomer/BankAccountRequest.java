package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record BankAccountRequest(
    UUID id,
    @NotBlank String bankCode,
    @NotBlank String agency,
    @NotBlank String accountNumber,
    String accountDigit,
    @NotBlank String accountType,
    @NotBlank String purpose,
    boolean isPrimary
) {
}
