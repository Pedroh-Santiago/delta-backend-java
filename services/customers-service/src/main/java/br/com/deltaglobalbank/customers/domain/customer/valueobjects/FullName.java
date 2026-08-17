package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

public record FullName(String value) {
    public FullName {
        String fullName = value.trim();
        if (fullName.isBlank()) {
            throw new IllegalArgumentException("full_name_blank");
        }
        if (fullName.length() < 3 || fullName.length() > 255) {
            throw new IllegalArgumentException("full_name_invalid_length");
        }
        if (fullName.split("\\s+").length < 2) {
            throw new IllegalArgumentException("full_name_incomplete");
        }
        value = fullName;
    }
}
