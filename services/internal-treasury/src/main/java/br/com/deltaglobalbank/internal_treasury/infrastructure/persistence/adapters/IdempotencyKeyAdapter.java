package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey;
import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyRepository;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.IdempotencyKeyEntity;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.IdempotencyKeyMapper;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories.JpaIdempotencyKeyRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class IdempotencyKeyAdapter implements IdempotencyRepository {

    private final JpaIdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyKeyAdapter(JpaIdempotencyKeyRepository idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Override
    public IdempotencyKey save(IdempotencyKey key) {
        return IdempotencyKeyMapper.toDomain(idempotencyKeyRepository.save(IdempotencyKeyMapper.toEntity(key)));
    }

    @Override
    public IdempotencyKey findByKey(UUID key) {
        IdempotencyKeyEntity entity = idempotencyKeyRepository.findById(key).orElse(null);
        return entity == null ? null : IdempotencyKeyMapper.toDomain(entity);
    }
}
