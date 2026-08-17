package br.com.deltaglobalbank.internal_treasury.domain.idempotency;

import java.util.UUID;

public interface IdempotencyRepository {
    IdempotencyKey save(IdempotencyKey key);

    IdempotencyKey findByKey(UUID key);
}
