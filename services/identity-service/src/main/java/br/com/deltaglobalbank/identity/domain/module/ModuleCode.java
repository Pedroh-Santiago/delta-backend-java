package br.com.deltaglobalbank.identity.domain.module;

public record ModuleCode(String value) {

    public static final int MAX_LENGTH = 100;

    public ModuleCode {
        if (value.isBlank()) {
            throw new IllegalArgumentException("module_code_blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("module_code_too_long");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
