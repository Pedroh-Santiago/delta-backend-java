package br.com.deltaglobalbank.identity.domain.user;

public record HashedPassword(String value) {

    public HashedPassword {
        if (value.isBlank()) {
            throw new IllegalArgumentException("hashed_password_blank");
        }
    }

    @Override
    public String toString() {
        return "HashedPassword(***)";
    }
}
