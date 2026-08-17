package br.com.deltaglobalbank.customers.domain.shared.valueobjects;

import java.util.Locale;

public record Uf(String value) {
    public Uf {
        String uf = value.trim().toUpperCase(Locale.ROOT);
        if (!uf.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("uf_invalid");
        }
        value = uf;
    }
}
