package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class BrazilianDate {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private BrazilianDate() {
    }

    public static String toBrDisplayOrRaw(String valor) {
        try {
            return LocalDate.parse(valor).format(FORMATTER);
        } catch (Exception ex) {
            return valor;
        }
    }
}
