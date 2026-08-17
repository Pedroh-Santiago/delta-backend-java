package br.com.deltaglobalbank.identity.features.ipAllowlist.listIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist.ListTenantIpAllowlistQuery
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist.ListTenantIpAllowlistUseCase
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist.ListedTenantIpAllowlist
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class listIpAllowlistUseCaseTests {
    @MockK
    lateinit var tenantRepository: TenantRepository

    @MockK
    lateinit var tenantIpAllowlistRepository: TenantIpAllowlistRepository

    private lateinit var useCase: ListTenantIpAllowlistUseCase

    @BeforeEach
    fun setUp() {
        useCase = ListTenantIpAllowlistUseCase(
            tenantIpAllowlistRepository,
            tenantRepository
        )
    }

    @Test
    fun `must throw TenantNotFoundException when tenant does not exist`() {
        val query = ListTenantIpAllowlistQuery(tenantId = UUID.randomUUID())
        every { tenantRepository.findById(any()) } returns null

        assertThrows(TenantNotFoundException::class.java) {
            useCase.execute(query)
        }
        verify(exactly = 0) { tenantIpAllowlistRepository.findAllByTenantId(any()) }
    }

    @Test
    fun `must return empty list when tenant has no entries`() {
        val tenantId = UUID.randomUUID()
        val query = ListTenantIpAllowlistQuery(tenantId = tenantId)

        every { tenantRepository.findById(tenantId) } returns mockk<Tenant>()
        every { tenantIpAllowlistRepository.findAllByTenantId(tenantId) } returns emptyList()

        val response = useCase.execute(query)

        assertEquals(emptyList<ListedTenantIpAllowlist>(), response.items)
    }

    @Test
    fun `must map all entries to response items`() {
        val tenantId = UUID.randomUUID()
        val query = ListTenantIpAllowlistQuery(tenantId = tenantId)

        every { tenantRepository.findById(tenantId) } returns mockk<Tenant>()

        val ip1 = mockk<TenantIpAllowlist>()
        every { ip1.id } returns UUID.randomUUID()
        every { ip1.tenantId } returns tenantId
        every { ip1.cidr } returns Cidr("192.168.0.0/24")
        every { ip1.description } returns "rede 1"
        every { ip1.createdAt } returns Instant.now()

        val ip2 = mockk<TenantIpAllowlist>()
        every { ip2.id } returns UUID.randomUUID()
        every { ip2.tenantId } returns tenantId
        every { ip2.cidr } returns Cidr("10.0.0.0/8")
        every { ip2.description } returns null
        every { ip2.createdAt } returns Instant.now()

        every { tenantIpAllowlistRepository.findAllByTenantId(tenantId) } returns listOf(ip1, ip2)

        val response = useCase.execute(query)

        assertAll(
            { assertEquals(2, response.items.size) },
            { assertEquals("192.168.0.0/24", response.items[0].cidr.value) },
            { assertEquals("rede 1", response.items[0].description) },
            { assertEquals(null, response.items[1].description) }
        )
    }
}