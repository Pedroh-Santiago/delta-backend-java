package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.IdempotencyKeyEntity;

public final class IdempotencyKeyMapper {

    private IdempotencyKeyMapper() {
    }

    public static IdempotencyKey toDomain(IdempotencyKeyEntity entity) {
        return new IdempotencyKey(
            entity.getKey(),
            entity.getResponse(),
            entity.getCreatedAt()
        );
    }

    public static IdempotencyKeyEntity toEntity(IdempotencyKey domain) {
        return new IdempotencyKeyEntity(
            domain.key(),
            domain.response(),
            domain.createdAt()
        );
    }
}
