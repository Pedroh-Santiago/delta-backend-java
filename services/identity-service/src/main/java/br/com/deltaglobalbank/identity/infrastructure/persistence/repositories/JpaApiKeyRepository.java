package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {
    ApiKeyEntity findByFingerprint(String fingerprint);

    List<ApiKeyEntity> findAllByApiClientIdIn(Set<UUID> apiClientIds);

    Page<ApiKeyEntity> findByApiClientId(UUID apiClientId, Pageable pageable);

    @Query("""
    SELECT k.apiClientId AS apiClientId, COUNT(k) AS total
    FROM ApiKeyEntity k
    WHERE k.apiClientId IN :clientIds
      AND k.revokedAt IS NULL
      AND (k.expiresAt IS NULL OR k.expiresAt > :now)
    GROUP BY k.apiClientId
    """)
    List<ApiKeyActiveCount> countActiveByApiClientIdIn(Set<UUID> clientIds, Instant now);
}
