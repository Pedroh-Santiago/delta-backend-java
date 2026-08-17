package br.com.deltaglobalbank.identity.domain.role;

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

    @Override
    public String toString() {
        return value;
    }
}
