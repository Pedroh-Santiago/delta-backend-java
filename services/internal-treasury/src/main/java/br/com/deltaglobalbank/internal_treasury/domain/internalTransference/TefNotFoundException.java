package br.com.deltaglobalbank.internal_treasury.domain.internalTransference;

import java.util.UUID;

public class TefNotFoundException extends RuntimeException {
    public TefNotFoundException(UUID id) {
        super("TEF " + id + " não encontrada");
    }
}
