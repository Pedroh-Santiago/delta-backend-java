package br.com.deltaglobalbank.identity.domain.tenant;

import java.util.Arrays;

public enum TenantStatus {
    ACTIVE,
    SUSPENDED,
    INACTIVE;

    public String toDatabaseValue() {
        return name().toLowerCase();
    }

    public static TenantStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Status de tenant inválido: " + value));
    }
}
