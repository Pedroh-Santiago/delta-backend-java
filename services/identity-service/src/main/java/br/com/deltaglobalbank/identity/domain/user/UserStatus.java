package br.com.deltaglobalbank.identity.domain.user;

import java.util.Arrays;

public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    LOCKED;

    public String toDatabaseValue() {
        return name().toLowerCase();
    }

    public static UserStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Status de user inválido: " + value));
    }
}
