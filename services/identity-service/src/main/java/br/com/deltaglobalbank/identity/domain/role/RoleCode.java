package br.com.deltaglobalbank.identity.domain.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record RoleCode(String value) {

    public static final int MAX_LENGTH = 100;

    public RoleCode {
        if (value.isBlank()) {
            throw new IllegalArgumentException("role_code_blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("role_code_too_long");
        }
    }

    @JsonCreator
    public static RoleCode of(String value) {
        return new RoleCode(value);
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
