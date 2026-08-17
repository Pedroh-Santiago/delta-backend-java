package br.com.deltaglobalbank.identity.domain.user;

import java.util.regex.Pattern;

public record Email(String value) {

    public static final int MAX_LENGTH = 255;
    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        String normalizedEmail = value.trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("email_blank");
        }
        if (normalizedEmail.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("email_too_long");
        }
        if (!EMAIL_REGEX.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("email_invalid");
        }
        value = normalizedEmail;
    }

    @Override
    public String toString() {
        return value;
    }
}
