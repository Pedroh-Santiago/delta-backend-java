package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

public final class SWorksFieldFormats {

    private SWorksFieldFormats() {
    }

    public static String apenasSeNumerico(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.chars().allMatch(Character::isDigit) ? valor : null;
    }
}
