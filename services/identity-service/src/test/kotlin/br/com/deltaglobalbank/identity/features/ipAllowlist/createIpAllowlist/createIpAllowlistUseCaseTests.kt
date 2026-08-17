package br.com.deltaglobalbank.identity.features.ipAllowlist.createIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.CidrAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.InvalidCidrException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist.CreateTenantIpAllowlistCommand
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist.CreateTenantIpAllowlistUsecase
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class createIpAllowlistUseCaseTests {

    @MockK
    lateinit var tenantRepository: TenantRepository

    @MockK
    lateinit var tenantIpAllowlistRepository: TenantIpAllowlistRepository

    @MockK
    lateinit var tenantIpAllowlistCache: TenantIpAllowlistCache

    private lateinit var useCase: CreateTenantIpAllowlistUsecase

    @BeforeEach
    fun setUp() {
        useCase = CreateTenantIpAllowlistUsecase(
            tenantIpAllowlistRepository,
            tenantRepository,
            tenantIpAllowlistCache
        )
    }

    @Test
    fun `must create ip allowlist successfully`() {
        val tenantId = UUID.randomUUID()
        val command = CreateTenantIpAllowlistCommand(
            tenantId = tenantId,
            cidr = "192.168.0.0/24",
            description = "allowed network"
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        every { tenantIpAllowlistRepository.existsByCidrAndTenantId(any(), tenantId) } returns false

        every { tenantIpAllowlistRepository.save(any()) } returns mockk(relaxed = true)
        every { tenantIpAllowlistCache.invalidate(any()) } returns Unit

        val response = useCase.execute(command)

        assertAll(
            { assertEquals(tenantId, response.tenantId) },
            { assertEquals("allowed network", response.description) }
        )
        verify(exactly = 1) { tenantIpAllowlistRepository.save(any()) }
    }

    @Test
    fun `must throw InvalidCidrException when cidr is invalid`() {
        val command = CreateTenantIpAllowlistCommand(
            tenantId = UUID.randomUUID(),
            cidr = "not.a.cidr/24",
            description = null
        )
        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(any()) } returns tenant

        assertThrows(InvalidCidrException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { tenantIpAllowlistRepository.save(any()) }
    }

    @Test
    fun `must detect duplicate cidr after normalization`() {
        val tenantId = UUID.randomUUID()
        val command = CreateTenantIpAllowlistCommand(
            tenantId = tenantId,
            cidr = "192.168.0.5/24",
            description = null
        )
        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        every { tenantIpAllowlistRepository.existsByCidrAndTenantId("192.168.0.0/24", tenantId) } returns true

        assertThrows(CidrAlreadyExistsException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { tenantIpAllowlistRepository.save(any()) }
    }

    @Test
    fun `must invalidate cache after creating allowlist entry`() {
        val tenantId = UUID.randomUUID()
        val command = CreateTenantIpAllowlistCommand(
            tenantId = tenantId,
            cidr = "192.168.0.0/24",
            description = null
        )
        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant
        every { tenantIpAllowlistRepository.existsByCidrAndTenantId(any(), tenantId) } returns false
        every { tenantIpAllowlistRepository.save(any()) } returns mockk(relaxed = true)
        every { tenantIpAllowlistCache.invalidate(tenantId) } returns Unit

        useCase.execute(command)

        verify(exactly = 1) { tenantIpAllowlistCache.invalidate(tenantId) }
    }
}