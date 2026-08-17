package br.com.deltaglobalbank.internal_treasury.domain.idempotency;

import java.time.Instant;
import java.util.UUID;

public class IdempotencyKey {

    private final UUID key;
    private final String response;
    private final Instant createdAt;

    public IdempotencyKey(UUID key, String response, Instant createdAt) {
        this.key = key;
        this.response = response;
        this.createdAt = createdAt;
    }

    public UUID key() {
        return key;
    }

    public String response() {
        return response;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
