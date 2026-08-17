package br.com.deltaglobalbank.customers.domain.customer;

import java.util.Arrays;
import java.util.Locale;

public enum CustomerStatus {
    ACTIVE,
    INACTIVE;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CustomerStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("invalid costumer status: " + value));
    }
}
