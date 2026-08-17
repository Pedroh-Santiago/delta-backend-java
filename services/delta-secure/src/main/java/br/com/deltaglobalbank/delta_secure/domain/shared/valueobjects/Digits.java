package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import java.util.regex.Pattern;

public final class Digits {

    private static final Pattern NAO_DIGITOS = Pattern.compile("\\D");

    private Digits() {
    }

    public static String onlyDigits(String valor) {
        return NAO_DIGITOS.matcher(valor).replaceAll("");
    }
}
