package br.com.deltaglobalbank.identity.features.apiClient.revokeApiKey

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyNotFoundException
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository
import br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey.RevokeApiKeyCommand
import br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey.RevokeApiKeyUseCase
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class RevokeApiKeyUseCaseTests {

    @MockK
    lateinit var apiKeyRepository: ApiKeyRepository

    @MockK
    lateinit var apiClientRepository: ApiClientRepository

    @MockK
    lateinit var cache: ExchangeApiKeyCache

    private lateinit var useCase: RevokeApiKeyUseCase

    @BeforeEach
    fun setUp() {
        useCase = RevokeApiKeyUseCase(
            apiKeyRepository,
            apiClientRepository,
            cache
        )
    }

    @Test
    fun `must revoke api key and invalidate cache when tenant matches`() {
        val apiKeyId = UUID.randomUUID()
        val apiClientId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val fingerprint = "fingerprint"
        val command = RevokeApiKeyCommand(
            apiKeyId = apiKeyId,
            tenantId = tenantId,
            revokedBy = UUID.randomUUID()
        )

        val apiKey = mockk<ApiKey>(relaxed = true)
        every { apiKey.apiClientId } returns apiClientId
        every { apiKey.fingerprint } returns fingerprint
        every { apiKey.id } returns apiKeyId
        every { apiKeyRepository.findById(apiKeyId) } returns apiKey

        val apiClient = mockk<ApiClient>()
        every { apiClient.tenantId } returns tenantId
        every { apiClientRepository.findById(apiClientId) } returns apiClient

        every { apiKeyRepository.save(apiKey) } returns apiKey
        every { cache.invalidate(fingerprint) } returns Unit

        useCase.execute(command)

        verify(exactly = 1) { apiKey.revoke() }
        verify(exactly = 1) { apiKeyRepository.save(apiKey) }
        verify(exactly = 1) { cache.invalidate(fingerprint) }
    }

    @Test
    fun `must revoke without tenant check when tenantId is null`() {
        val apiKeyId = UUID.randomUUID()
        val fingerprint = "fingerprint"
        val command = RevokeApiKeyCommand(
            apiKeyId = apiKeyId,
            tenantId = null,
            revokedBy = UUID.randomUUID()
        )
        val apiKey = mockk<ApiKey>(relaxed = true)
        every { apiKey.fingerprint } returns fingerprint
        every { apiKey.id } returns apiKeyId
        every { apiKeyRepository.findById(apiKeyId) } returns apiKey
        every { apiKeyRepository.save(apiKey) } returns apiKey
        every { cache.invalidate(fingerprint) } returns Unit

        useCase.execute(command)

        verify(exactly = 0) { apiClientRepository.findById(any()) }
        verify(exactly = 1) { apiKey.revoke() }
        verify(exactly = 1) { cache.invalidate(fingerprint) }
    }

    @Test
    fun `must throw when api key belongs to a different tenant`() {
        val apiKeyId = UUID.randomUUID()
        val apiClientId = UUID.randomUUID()
        val ownerTenantId = UUID.randomUUID()
        val attackerTenantId = UUID.randomUUID()
        val command = RevokeApiKeyCommand(
            apiKeyId = apiKeyId,
            tenantId = attackerTenantId,
            revokedBy = UUID.randomUUID()
        )
        val apiKey = mockk<ApiKey>()
        every { apiKey.apiClientId } returns apiClientId
        every { apiKeyRepository.findById(apiKeyId) } returns apiKey

        val apiClient = mockk<ApiClient>()
        every { apiClient.tenantId } returns ownerTenantId
        every { apiClientRepository.findById(apiClientId) } returns apiClient

        assertThrows(ApiKeyNotFoundException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiKeyRepository.save(any()) }
        verify(exactly = 0) { cache.invalidate(any()) }
    }

    @Test
    fun `must throw when api key does not exist`() {
        val command = RevokeApiKeyCommand(
            apiKeyId = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            revokedBy = UUID.randomUUID()
        )
        every { apiKeyRepository.findById(command.apiKeyId) } returns null

        assertThrows(ApiKeyNotFoundException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { apiKeyRepository.save(any()) }
        verify(exactly = 0) { cache.invalidate(any()) }
    }

    @Test
    fun `must throw when api client does not exist`() {
        val apiKeyId = UUID.randomUUID()
        val apiClientId = UUID.randomUUID()
        val command = RevokeApiKeyCommand(
            apiKeyId = apiKeyId,
            tenantId = UUID.randomUUID(),
            revokedBy = UUID.randomUUID()
        )
        val apiKey = mockk<ApiKey>()
        every { apiKey.apiClientId } returns apiClientId
        every { apiKeyRepository.findById(apiKeyId) } returns apiKey

        every { apiClientRepository.findById(apiClientId) } returns null

        assertThrows(ApiKeyNotFoundException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { apiKeyRepository.save(any()) }
        verify(exactly = 0) { cache.invalidate(any()) }
    }
}