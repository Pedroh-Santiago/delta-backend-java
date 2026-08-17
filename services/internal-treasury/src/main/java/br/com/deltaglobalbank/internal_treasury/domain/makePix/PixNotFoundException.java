package br.com.deltaglobalbank.internal_treasury.domain.makePix;

import java.util.UUID;

public class PixNotFoundException extends RuntimeException {
    public PixNotFoundException(UUID id) {
        super("PIX " + id + " não foi encontrado");
    }
}
