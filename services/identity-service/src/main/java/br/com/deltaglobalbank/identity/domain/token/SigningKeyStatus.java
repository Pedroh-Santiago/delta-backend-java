package br.com.deltaglobalbank.identity.domain.token;

import java.util.Arrays;

public enum SigningKeyStatus {
    ACTIVE,
    RETIRED,
    REVOKED;

    public String toDatabaseValue() {
        return name().toLowerCase();
    }

    public static SigningKeyStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("signing_key_status_invalid: " + value));
    }
}
