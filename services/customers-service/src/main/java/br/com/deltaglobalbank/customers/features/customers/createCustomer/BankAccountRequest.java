package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import jakarta.validation.constraints.NotBlank;

public record BankAccountRequest(
    @NotBlank String bankCode,
    @NotBlank String agency,
    @NotBlank String accountNumber,
    String accountDigit,
    @NotBlank String accountType,
    @NotBlank String purpose,
    boolean isPrimary
) {
}
