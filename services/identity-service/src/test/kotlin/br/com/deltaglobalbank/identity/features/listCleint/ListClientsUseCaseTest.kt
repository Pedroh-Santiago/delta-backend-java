package br.com.deltaglobalbank.identity.features.listCleint


import io.mockk.every
import io.mockk.verify
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.features.apiClients.listClients.ListClientsUseCase
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.ApiKeyActiveCount
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class ListClientsUseCaseTest {

    @MockK
    lateinit var jpaApiClientRepository: JpaApiClientRepository

    @MockK
    lateinit var tenantRepository: JpaTenantRepository

    @MockK
    lateinit var jpaApiClientRoleRepository : JpaApiClientRoleRepository

    @MockK
    lateinit var roleRepository: RoleRepository

    @MockK
    lateinit var jpaApiKeyRepository : JpaApiKeyRepository

    private lateinit var useCase: ListClientsUseCase

    @BeforeEach
    fun setUp() {
        useCase = ListClientsUseCase(
            jpaApiClientRepository,
            tenantRepository,
            jpaApiClientRoleRepository,
            roleRepository,
            jpaApiKeyRepository,
        )
    }

    @Test
    fun `should map activeKeysCount from active key count query`(){
        val tenantId = UUID.randomUUID()
        val clientId = UUID.randomUUID()

        val client = mockk<ApiClientEntity>()
        every { client.id } returns clientId
        every { client.tenantId } returns tenantId
        every { client.name } returns "ERP"
        every { client.description } returns null
        every { client.status } returns "active"
        every { client.createdAt } returns Instant.now()
        every { jpaApiClientRepository.findByTenantId(tenantId, any()) } returns
                PageImpl(listOf(client), PageRequest.of(0, 20), 1)

        val tenant = mockk<TenantEntity>()
        every { tenant.slug } returns "empresa"
        every { tenantRepository.findById(tenantId) } returns Optional.of(tenant)

        every { jpaApiClientRoleRepository.findAllByApiClientIdIn(any()) } returns emptyList()

        val count = mockk<ApiKeyActiveCount>()
        every { count.apiClientId } returns clientId
        every { count.total } returns 2L
        every { jpaApiKeyRepository.countActiveByApiClientIdIn(any(), any()) } returns listOf(count)

        val response = useCase.listClients(tenantId, 0, 20)

        verify(exactly = 1) { jpaApiKeyRepository.countActiveByApiClientIdIn(any(), any()) }

        assertEquals(1, response.items.size)
        assertEquals(2, response.items[0].activeKeysCount)
        assertEquals("empresa", response.items[0].tenantSlug)
    }

    @Test
    fun `should map pagination metadata from page`(){
        val tenantId = UUID.randomUUID()
        val clientId = UUID.randomUUID()

        val client = mockk<ApiClientEntity>()
        every { client.id } returns clientId
        every { client.tenantId } returns tenantId
        every { client.name } returns "ERP"
        every { client.description } returns null
        every { client.status } returns "active"
        every { client.createdAt } returns Instant.now()

        every { jpaApiClientRepository.findByTenantId(tenantId, any()) } returns
                PageImpl(listOf(client), PageRequest.of(0, 5), 12)

        val tenant = mockk<TenantEntity>()
        every { tenant.slug } returns "empresa"
        every { tenantRepository.findById(tenantId) } returns Optional.of(tenant)
        every { jpaApiClientRoleRepository.findAllByApiClientIdIn(any()) } returns emptyList()
        every { jpaApiKeyRepository.countActiveByApiClientIdIn(any(), any()) } returns emptyList()

        val response = useCase.listClients(tenantId, 2, 5)

        assertEquals(2, response.page)
        assertEquals(5, response.size)
        assertEquals(12, response.totalElements)
        assertEquals(3, response.totalPages)
    }



}