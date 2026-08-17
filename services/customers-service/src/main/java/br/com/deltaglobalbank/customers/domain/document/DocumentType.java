package br.com.deltaglobalbank.customers.domain.document;

import java.util.Arrays;
import java.util.Locale;

public enum DocumentType {
    RG,
    CNH;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static DocumentType fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("invalid DocumentType: " + value));
    }
}
