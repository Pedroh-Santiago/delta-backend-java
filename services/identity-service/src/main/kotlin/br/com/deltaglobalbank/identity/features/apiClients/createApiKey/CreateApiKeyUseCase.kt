package br.com.deltaglobalbank.identity.features.apiClients.createApiKey

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientSuspendedException
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository
import br.com.deltaglobalbank.identity.domain.apiKey.TenantInactiveForApiKeyException
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.Password
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyFingerprinter
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyGenerator
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class CreateApiKeyCommand(
    val apiClientId: UUID,
    val expectedTenantId: UUID?,
    val name: String,
    val expiresAt: Instant?
)

@Service
class CreateApiKeyUseCase(
    private val apiClientRepository: ApiClientRepository,
    private val apiKeyRepository: ApiKeyRepository,
    private val tenantRepository: TenantRepository,
    private val passwordHasher: PasswordHasher,
    private val apiKeyGenerator: ApiKeyGenerator,
    private val apiKeyFingerprinter: ApiKeyFingerprinter
) {

    @Transactional
    fun execute(command: CreateApiKeyCommand): CreateApiKeyResponse {
        val apiClient = apiClientRepository.findById(command.apiClientId)
            ?: throw ApiClientNotFoundException()

        if (command.expectedTenantId != null && apiClient.tenantId != command.expectedTenantId) {
            throw ApiClientNotFoundException()
        }

        if (!apiClient.isActive()) {
            throw ApiClientSuspendedException()
        }

        val tenant = tenantRepository.findById(apiClient.tenantId)
            ?: throw ApiClientNotFoundException()
        if (!tenant.isActive()) {
            throw TenantInactiveForApiKeyException()
        }

        val generated = apiKeyGenerator.generate()
        val keyHash = passwordHasher.hash(Password(generated.plainKey))
        val fingerprint = apiKeyFingerprinter.fingerprint(generated.plainKey)

        val apiKey = ApiKey.create(
            id = UuidCreator.getTimeOrderedEpoch(),
            apiClientId = apiClient.id,
            name = command.name,
            keyHash = keyHash,
            keyPrefix = generated.prefix,
            fingerprint = fingerprint,
            expiresAt = command.expiresAt
        )

        val saved = apiKeyRepository.save(apiKey)

        return CreateApiKeyResponse(
            apiKey = CreatedApiKey(
                id = saved.id,
                apiClientId = saved.apiClientId,
                name = saved.name,
                keyPrefix = saved.keyPrefix,
                expiresAt = saved.snapshot().expiresAt,
                createdAt = saved.createdAt
            ),
            key = generated.plainKey
        )
    }
}