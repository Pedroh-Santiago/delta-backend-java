package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories;

import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JpaIdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, UUID> {
}
