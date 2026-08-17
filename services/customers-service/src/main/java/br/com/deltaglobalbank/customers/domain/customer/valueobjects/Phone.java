package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

public record Phone(String value) {
    public Phone {
        if (!value.matches("^\\+\\d{8,15}$")) {
            throw new IllegalArgumentException("phone_invalid");
        }
    }
}
