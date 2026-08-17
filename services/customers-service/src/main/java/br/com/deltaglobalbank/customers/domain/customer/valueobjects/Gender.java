package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import java.util.Arrays;
import java.util.Locale;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Gender fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(gender -> gender.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Gender invalid: " + value));
    }
}
