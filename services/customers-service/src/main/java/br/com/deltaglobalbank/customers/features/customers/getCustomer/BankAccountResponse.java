package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import java.util.UUID;

public record BankAccountResponse(
    UUID id,
    String bankCode,
    String agency,
    String accountNumber,
    String accountDigit,
    String accountType,
    String purpose,
    boolean isPrimary
) {
}
