package br.com.deltaglobalbank.identity.domain.apiClient;

import java.util.Arrays;

public enum ApiClientStatus {
    ACTIVE,
    SUSPENDED;

    public String toDatabaseValue() {
        return name().toLowerCase();
    }

    public static ApiClientStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Status de API CLIENT inválido: " + value));
    }
}
