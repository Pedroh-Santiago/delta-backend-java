package br.com.deltaglobalbank.identity.domain.user;

public record Password(String value) {

    public static final int MIN_LENGTH = 8;

    public Password {
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("password_too_short");
        }
        if (value.chars().noneMatch(Character::isLetter)) {
            throw new IllegalArgumentException("password_missing_letter");
        }
        if (value.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("password_missing_digit");
        }
    }

    @Override
    public String toString() {
        return "Password(***)";
    }
}
