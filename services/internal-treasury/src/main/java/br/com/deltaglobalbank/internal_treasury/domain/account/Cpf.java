package br.com.deltaglobalbank.internal_treasury.domain.account;

public record Cpf(String value) {

    public static final int MAX_LENGTH = 11;

    public Cpf {
        if (value.isBlank()) {
            throw new IllegalArgumentException("Cpf_notBlank");
        }
        if (value.length() != MAX_LENGTH) {
            throw new IllegalArgumentException("Cpf_length is not " + MAX_LENGTH);
        }
        if (!value.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Cpf_onlyDigits");
        }
        if (!isValid(value)) {
            throw new IllegalArgumentException("Cpf_invalid");
        }
    }

    private static int calculateDigit(String cpfPart, int initialWeight) {
        int sum = 0;
        int weight = initialWeight;

        for (int i = 0; i < cpfPart.length(); i++) {
            sum += Character.digit(cpfPart.charAt(i), 10) * weight;
            weight--;
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean isValid(String cpf) {

        boolean allEqual = true;
        for (int i = 0; i < cpf.length(); i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        int firstDigit = calculateDigit(cpf.substring(0, 9), 10);
        int secondDigit = calculateDigit(cpf.substring(0, 10), 11);

        return firstDigit == Character.digit(cpf.charAt(9), 10)
                && secondDigit == Character.digit(cpf.charAt(10), 10);
    }
}
