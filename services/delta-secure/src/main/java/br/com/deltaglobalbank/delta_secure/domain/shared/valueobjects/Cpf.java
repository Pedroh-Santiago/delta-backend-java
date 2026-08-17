package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

public record Cpf(String value) {

    public String digitsOnly() {
        return Digits.onlyDigits(value);
    }

    public String masked() {
        String digits = digitsOnly();
        if (digits.length() != 11) {
            return value;
        }
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
    }
}
