package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ListApiKeyClientUseCase(private val apiClientRepository : ApiClientRepository, private val jpaApiKeyRepository : JpaApiKeyRepository) {

    companion object { const val MAX_PAGE_SIZE = 100}

    @Transactional
    fun getClientApiKey(apiClientId: UUID, tenantId: UUID, page: Int, size: Int): ClientApiKeyResponse {
        val apiClient =  apiClientRepository.findById(apiClientId) ?: throw ApiClientNotFoundException()
        if (apiClient.tenantId != tenantId) throw ApiClientNotFoundException()

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val pageable = PageRequest.of(safePage, safeSize, Sort.Direction.DESC, "createdAt")

        val apiKeysPage = jpaApiKeyRepository.findByApiClientId(apiClientId, pageable)

        val items = apiKeysPage.content.map { key -> ApiKeyItemsResponse(
            id = key.id,
            name = key.name,
            keyPrefix = key.keyPrefix,
            expiresAt = key.expiresAt,
            revokedAt = key.revokedAt,
            lastUsedAt = key.lastUsedAt,
            createdAt = key.createdAt
        ) }

        return ClientApiKeyResponse(items)
    }
}