package br.com.deltaglobalbank.identity.features.ipAllowlist.deleteIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistEntryNotFoundException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist.DeleteTenantIpAllowlistCommand
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist.DeleteTenantIpAllowlistUseCase
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class deleteIpAllowlistUseCaseTests {
    @MockK
    lateinit var tenantIpAllowlistRepository: TenantIpAllowlistRepository

    @MockK
    lateinit var tenantIpAllowlistCache: TenantIpAllowlistCache

    private lateinit var useCase: DeleteTenantIpAllowlistUseCase

    @BeforeEach
    fun setUp() {
        useCase = DeleteTenantIpAllowlistUseCase(
            tenantIpAllowlistRepository,
            tenantIpAllowlistCache

        )
    }

    @Test
    fun `must throw when entry does not exist`() {
        val command = DeleteTenantIpAllowlistCommand(
            id = UUID.randomUUID(),
            tenantId = UUID.randomUUID()
        )
        every { tenantIpAllowlistRepository.findById(command.id) } returns null

        assertThrows(IpAllowlistEntryNotFoundException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { tenantIpAllowlistRepository.delete(any()) }
    }

    @Test
    fun `must throw when entry belongs to a different tenant`() {
        val entryId = UUID.randomUUID()
        val ownerTenantId = UUID.randomUUID()
        val attackerTenantId = UUID.randomUUID()
        val command = DeleteTenantIpAllowlistCommand(
            id = entryId,
            tenantId = attackerTenantId
        )

        val entry = mockk<TenantIpAllowlist>()
        every { entry.tenantId } returns ownerTenantId
        every { tenantIpAllowlistRepository.findById(entryId) } returns entry

        assertThrows(IpAllowlistEntryNotFoundException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { tenantIpAllowlistRepository.delete(any()) }
    }

    @Test
    fun `must delete entry when it exists and belongs to the tenant`() {
        val entryId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val command = DeleteTenantIpAllowlistCommand(id = entryId, tenantId = tenantId)

        val entry = mockk<TenantIpAllowlist>()
        every { entry.tenantId } returns tenantId
        every { tenantIpAllowlistRepository.findById(entryId) } returns entry
        every { tenantIpAllowlistRepository.delete(entryId) } just Runs
        every { tenantIpAllowlistCache.invalidate(tenantId) } returns Unit

        useCase.execute(command)

        verify(exactly = 1) { tenantIpAllowlistRepository.delete(entryId) }
        verify(exactly = 1) { tenantIpAllowlistCache.invalidate(tenantId) }
    }
}