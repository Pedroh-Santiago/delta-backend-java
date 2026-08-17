package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import java.util.Arrays;
import java.util.Locale;

public enum MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOWED,
    STABLE_UNION;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static MaritalStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Marital status invalid: " + value));
    }
}
