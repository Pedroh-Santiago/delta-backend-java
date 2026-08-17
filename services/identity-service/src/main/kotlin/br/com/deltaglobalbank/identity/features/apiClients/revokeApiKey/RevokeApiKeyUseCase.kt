package br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyNotFoundException
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

data class RevokeApiKeyCommand(
    val apiKeyId: UUID,
    val tenantId: UUID?,
    val revokedBy: UUID
)

@Service
class RevokeApiKeyUseCase(
    private val apiKeyRepository: ApiKeyRepository,
    private val apiClientRepository: ApiClientRepository,
    private val cache: ExchangeApiKeyCache
){
    fun execute(command: RevokeApiKeyCommand){
        val apiKey = apiKeyRepository.findById(command.apiKeyId)
            ?: throw ApiKeyNotFoundException()

        if (command.tenantId != null){
            val apiClient = apiClientRepository.findById(apiKey.apiClientId)
                ?: throw ApiKeyNotFoundException()
            if (apiClient.tenantId != command.tenantId){
                throw ApiKeyNotFoundException()
            }
        }

        apiKey.revoke()
        apiKeyRepository.save(apiKey)
        cache.invalidate(apiKey.fingerprint)

        val log = LoggerFactory.getLogger(javaClass)
        log.info("api_key revoked apiKeyId={} revokedBy={} at={}",
            apiKey.id, command.revokedBy, Instant.now())
    }
}