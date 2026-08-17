package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ApiClientRepositoryAdapter(
    private val jpaApiClientRepository: JpaApiClientRepository
) : ApiClientRepository {

    override fun findById(id: UUID): ApiClient? =
        jpaApiClientRepository.findById(id).orElse(null)?.toDomain()

    override fun save(apiClient: ApiClient): ApiClient {
        val existing = jpaApiClientRepository.findById(apiClient.id).orElse(null)
        val toSave = if (existing != null) apiClient.applyTo(existing) else apiClient.toEntity()
        return jpaApiClientRepository.save(toSave).toDomain()
    }
}