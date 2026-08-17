package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ApiKeyRepositoryAdapter(
    private val jpaRepository: JpaApiKeyRepository
) : ApiKeyRepository {

    override fun save(apiKey: ApiKey): ApiKey {
        val existing = jpaRepository.findById(apiKey.id).orElse(null)
        return if (existing == null) {
            jpaRepository.save(apiKey.toEntity()).toDomain()
        } else {
            apiKey.applyTo(existing)
            jpaRepository.save(existing).toDomain()
        }
    }

    override fun findById(id: UUID): ApiKey? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByFingerprint(fingerprint: String): ApiKey? =
        jpaRepository.findByFingerprint(fingerprint)?.toDomain()
}