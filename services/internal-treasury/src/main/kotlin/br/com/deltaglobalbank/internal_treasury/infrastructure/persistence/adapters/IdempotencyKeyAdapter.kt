package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey
import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyRepository
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories.JpaIdempotencyKeyRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IdempotencyKeyAdapter (
    private val idempotencyKeyRepository : JpaIdempotencyKeyRepository
) : IdempotencyRepository {

    override fun save(key: IdempotencyKey): IdempotencyKey {
        return idempotencyKeyRepository.save(key.toEntity()).toDomain()
    }

    override fun findByKey(key: UUID): IdempotencyKey? {
        return idempotencyKeyRepository.findById(key).orElse(null)?.toDomain()
    }

}