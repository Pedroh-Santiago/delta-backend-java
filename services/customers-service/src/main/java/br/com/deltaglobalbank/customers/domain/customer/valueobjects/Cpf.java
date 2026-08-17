package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

public record Cpf(String value) {
    public Cpf {
        String digitsOnly = value.replaceAll("\\D", "");
        if (digitsOnly.length() != 11) {
            throw new IllegalArgumentException("invalid_cpf");
        }
        if (digitsOnly.chars().distinct().count() <= 1) {
            throw new IllegalArgumentException("invalid_cpf");
        }
        int[] digits = digitsOnly.chars().map(c -> c - '0').toArray();
        int firstVerifierDigit = checkDigit(digits, 9, 10);
        int secondVerifierDigit = checkDigit(digits, 10, 11);
        if (!(firstVerifierDigit == digits[9] && secondVerifierDigit == digits[10])) {
            throw new IllegalArgumentException("invalid_cpf");
        }
        value = digitsOnly;
    }

    private static int checkDigit(int[] digits, int count, int startWeight) {
        int weightedSum = 0;
        for (int i = 0; i < count; i++) {
            weightedSum += digits[i] * (startWeight - i);
        }
        int remainder = weightedSum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
