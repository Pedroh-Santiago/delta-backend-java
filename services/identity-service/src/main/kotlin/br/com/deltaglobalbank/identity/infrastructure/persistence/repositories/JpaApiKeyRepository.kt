package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface JpaApiKeyRepository : JpaRepository<ApiKeyEntity, UUID> {
    fun findByFingerprint(fingerprint: String): ApiKeyEntity?

    fun findAllByApiClientIdIn(apiClientIds: Set<UUID>): List<ApiKeyEntity>

    fun findByApiClientId(apiClientId: UUID, pageable: Pageable): Page<ApiKeyEntity>

    @Query("""
    SELECT k.apiClientId AS apiClientId, COUNT(k) AS total
    FROM ApiKeyEntity k
    WHERE k.apiClientId IN :clientIds
      AND k.revokedAt IS NULL
      AND (k.expiresAt IS NULL OR k.expiresAt > :now)
    GROUP BY k.apiClientId
    """)
    fun countActiveByApiClientIdIn(clientIds: Set<UUID>, now: Instant): List<ApiKeyActiveCount>
}