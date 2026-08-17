package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaIssuedTokenAuditRepository extends JpaRepository<IssuedTokenAuditEntity, UUID> {
    IssuedTokenAuditEntity findByJti(UUID jti);
}
