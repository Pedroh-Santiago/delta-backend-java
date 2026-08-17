package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects;

public record Agency(String value) {
    public Agency {
        if (!value.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("agency_invalid");
        }
    }
}
