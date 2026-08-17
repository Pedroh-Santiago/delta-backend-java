package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

    @Id
    private UUID key;

    @Column(name = "response", nullable = false)
    private String response;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKeyEntity() {
    }

    public IdempotencyKeyEntity(UUID key, String response, Instant createdAt) {
        this.key = key;
        this.response = response;
        this.createdAt = createdAt;
    }

    public UUID getKey() {
        return key;
    }

    public String getResponse() {
        return response;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
