package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

public record MotherName(String value) {
    public MotherName {
        String motherName = value.trim();
        if (motherName.isBlank()) {
            throw new IllegalArgumentException("mother_name_blank");
        }
        if (motherName.length() > 255) {
            throw new IllegalArgumentException("mother_name_too_long");
        }
        value = motherName;
    }
}
