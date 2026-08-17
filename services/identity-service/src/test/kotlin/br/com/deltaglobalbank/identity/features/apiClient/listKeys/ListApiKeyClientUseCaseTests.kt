package br.com.deltaglobalbank.identity.features.apiClient.listKeys

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException
import br.com.deltaglobalbank.identity.features.apiClients.listApiKeys.ListApiKeyClientUseCase
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ListApiKeyClientUseCaseTests {


    @MockK
    lateinit var apiClientRepository : ApiClientRepository

    @MockK
    lateinit var jpaApiKeyRepository : JpaApiKeyRepository


    private lateinit var useCase: ListApiKeyClientUseCase

    @BeforeEach
    fun setUp() {
        useCase = ListApiKeyClientUseCase(
            apiClientRepository,
            jpaApiKeyRepository,
        )
    }

    @Test
    fun `should throw ApiClientNotFoundException when api client belongs to another tenant`() {
        val apiClientId = UUID.randomUUID()
        val actingTenantId = UUID.randomUUID()
        val otherTenantId = UUID.randomUUID()

        val apiClient = mockk<ApiClient>()
        every { apiClient.tenantId } returns otherTenantId
        every { apiClientRepository.findById(apiClientId) } returns apiClient

        assertThrows<ApiClientNotFoundException> {
            useCase.getClientApiKey(apiClientId, actingTenantId, 0, 20)
        }

        verify(exactly = 0) { jpaApiKeyRepository.findByApiClientId(any(), any()) }
    }

    @Test
    fun `should throw ApiClientNotFoundException when api client does not exist`() {
        val apiClientId = UUID.randomUUID()
        every { apiClientRepository.findById(apiClientId) } returns null

        assertThrows<ApiClientNotFoundException> {
            useCase.getClientApiKey(apiClientId, UUID.randomUUID(), 0, 20)
        }
        verify(exactly = 0) { jpaApiKeyRepository.findByApiClientId(any(), any()) }
    }

    @Test
    fun `should list all keys including revoked and expired with their state`() {
        val apiClientId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val now = Instant.now()

        val apiClient = mockk<ApiClient>()
        every { apiClient.tenantId } returns tenantId
        every { apiClientRepository.findById(apiClientId) } returns apiClient

        val activeKey = mockk<ApiKeyEntity>()
        every { activeKey.id } returns UUID.randomUUID()
        every { activeKey.name } returns "active-key"
        every { activeKey.keyPrefix } returns "dgb_live_a"
        every { activeKey.expiresAt } returns null
        every { activeKey.revokedAt } returns null
        every { activeKey.lastUsedAt } returns null
        every { activeKey.createdAt } returns now

        val revokedKey = mockk<ApiKeyEntity>()
        every { revokedKey.id } returns UUID.randomUUID()
        every { revokedKey.name } returns "revoked-key"
        every { revokedKey.keyPrefix } returns "dgb_live_b"
        every { revokedKey.expiresAt } returns null
        every { revokedKey.revokedAt } returns now.minusSeconds(3600)
        every { revokedKey.lastUsedAt } returns null
        every { revokedKey.createdAt } returns now.minusSeconds(7200)

        val expiredKey = mockk<ApiKeyEntity>()
        every { expiredKey.id } returns UUID.randomUUID()
        every { expiredKey.name } returns "expired-key"
        every { expiredKey.keyPrefix } returns "dgb_live_c"
        every { expiredKey.expiresAt } returns now.minusSeconds(3600)
        every { expiredKey.revokedAt } returns null
        every { expiredKey.lastUsedAt } returns null
        every { expiredKey.createdAt } returns now.minusSeconds(7200)

        val keys = listOf(activeKey, revokedKey, expiredKey)
        every { jpaApiKeyRepository.findByApiClientId(apiClientId, any()) } returns
                PageImpl(keys, PageRequest.of(0, 20), keys.size.toLong())

        val response = useCase.getClientApiKey(apiClientId, tenantId, 0, 20)

        Assertions.assertEquals(3, response.items.size)
        Assertions.assertEquals(1, response.items.count { it.revokedAt != null })
        Assertions.assertEquals(1, response.items.count { it.expiresAt != null && it.expiresAt.isBefore(now) })
    }

}