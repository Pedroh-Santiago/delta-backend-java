package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects;

public record BankCode(String value) {
    public BankCode {
        if (!value.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("bank_code_invalid");
        }
    }
}
