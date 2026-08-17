package br.com.deltaglobalbank.identity.domain.tenant;

import java.util.regex.Pattern;

public record TenantSlug(String value) {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 100;
    private static final Pattern SLUG_REGEX = Pattern.compile("^[a-z0-9-]+$");

    public TenantSlug {
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("invalid_slug_length");
        }
        if (!SLUG_REGEX.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid_slug_format");
        }
        if (value.startsWith("-") || value.endsWith("-")) {
            throw new IllegalArgumentException("invalid_slug_format");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
