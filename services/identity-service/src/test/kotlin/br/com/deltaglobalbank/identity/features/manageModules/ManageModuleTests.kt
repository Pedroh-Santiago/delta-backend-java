package br.com.deltaglobalbank.identity.features.manageModules

import br.com.deltaglobalbank.identity.domain.module.Module
import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantSlug
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.features.modules.disableModule.DisableTenantModuleCommand
import br.com.deltaglobalbank.identity.features.modules.disableModule.DisableTenantModuleUseCase
import br.com.deltaglobalbank.identity.features.modules.enableModule.EnableTenantModuleCommand
import br.com.deltaglobalbank.identity.features.modules.enableModule.EnableTenantModuleUseCase
import br.com.deltaglobalbank.identity.features.modules.listModules.ListTenantModulesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManageModuleTests {

    private val tenantRepository = mockk<TenantRepository>()
    private val moduleRepository = mockk<ModuleRepository>()
    private val tenantModuleRepository = mockk<TenantModuleRepository>()

    private val enableUseCase = EnableTenantModuleUseCase(
        tenantRepository, moduleRepository, tenantModuleRepository
    )
    private val disableUseCase = DisableTenantModuleUseCase(
        tenantRepository, moduleRepository, tenantModuleRepository
    )
    private val listUseCase = ListTenantModulesUseCase(
        tenantRepository, moduleRepository, tenantModuleRepository
    )

    private val tenantId = UUID.randomUUID()
    private val moduleId = UUID.randomUUID()
    private val cardModuleId = UUID.randomUUID()

    private val tenant = Tenant.create(
        id = tenantId,
        name = "Empresa Teste",
        slug = TenantSlug("empresa-teste")
    )

    private val lendingModule = Module(
        id = moduleId,
        code = ModuleCode("lending"),
        name = "Empréstimos",
        description = "teste",
        createdAt = Instant.now()
    )

    private val cardModule = Module(
        id = cardModuleId,
        code = ModuleCode("card"),
        name = "Cartão",
        description = "teste",
        createdAt = Instant.now()
    )

    @Test
    fun `module should be enabled`() {
        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findByCode(ModuleCode("lending")) } returns lendingModule
        every { tenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId) } returns null

        val savedSlot = slot<TenantModule>()
        every { tenantModuleRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val response = enableUseCase.execute(EnableTenantModuleCommand(tenantId, "lending"))

        assertEquals("lending", response.moduleCode)
        assertTrue(response.enabled)
        assertNotNull(response.enabledAt)
        assertTrue(savedSlot.captured.isEnabled())
    }

    @Test
    fun `module should be reactivated`() {
        val existingTenantModule = TenantModule.create(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            moduleId = cardModuleId
        )
        existingTenantModule.disable()
        assertFalse(existingTenantModule.isEnabled())

        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findByCode(ModuleCode("card")) } returns cardModule
        every {
            tenantModuleRepository.findByTenantIdAndModuleId(
                tenantId,
                cardModuleId
            )
        } returns existingTenantModule
        every { tenantModuleRepository.save(any()) } answers { firstArg() }

        val response = enableUseCase.execute(
            EnableTenantModuleCommand(tenantId = tenantId, moduleCode = "card")
        )

        assertTrue(response.enabled)
        assertTrue(existingTenantModule.isEnabled())
    }

    @Test
    fun `should return module_not_found when module does not exist`() {
        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findByCode(ModuleCode("inexistente")) } returns null

        assertThrows<ModuleNotFoundException> {
            enableUseCase.execute(
                EnableTenantModuleCommand(tenantId = tenantId, moduleCode = "inexistente")
            )
        }
    }

    @Test
    fun `should return tenant_not_found when tenant does not exist`() {
        val unknownTenantId = UUID.randomUUID()
        every { tenantRepository.findById(unknownTenantId) } returns null

        assertThrows<TenantNotFoundException> {
            enableUseCase.execute(
                EnableTenantModuleCommand(tenantId = unknownTenantId, moduleCode = "lending")
            )
        }
    }

    @Test
    fun `module should be disabled`() {
        val existingTenantModule = TenantModule.create(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            moduleId = moduleId
        )
        assertTrue(existingTenantModule.isEnabled())

        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findByCode(ModuleCode("lending")) } returns lendingModule
        every {
            tenantModuleRepository.findByTenantIdAndModuleId(
                tenantId,
                moduleId
            )
        } returns existingTenantModule
        every { tenantModuleRepository.save(any()) } answers { firstArg() }

        disableUseCase.execute(
            DisableTenantModuleCommand(tenantId = tenantId, moduleCode = "lending")
        )

        assertFalse(existingTenantModule.isEnabled())
        verify { tenantModuleRepository.save(existingTenantModule) }
    }

    @Test
    fun `disabling module without entry is idempotent`() {
        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findByCode(ModuleCode("card")) } returns cardModule
        every {
            tenantModuleRepository.findByTenantIdAndModuleId(
                tenantId,
                cardModuleId
            )
        } returns null

        disableUseCase.execute(
            DisableTenantModuleCommand(tenantId = tenantId, moduleCode = "card")
        )

        verify(exactly = 0) { tenantModuleRepository.save(any()) }
    }

    @Test
    fun `module should be listed`() {
        val lendingTm = TenantModule.create(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            moduleId = moduleId
        )
        val cardTm = TenantModule.create(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            moduleId = cardModuleId
        )
        cardTm.disable()

        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findAll() } returns listOf(lendingModule, cardModule)
        every { tenantModuleRepository.findAllByTenantId(tenantId) } returns listOf(
            lendingTm,
            cardTm
        )

        val response = listUseCase.execute(tenantId)

        assertEquals(2, response.items.size)
        val lendingItem = response.items.first { it.moduleCode == "lending" }
        val cardItem = response.items.first { it.moduleCode == "card" }
        assertTrue(lendingItem.enabled)
        assertFalse(cardItem.enabled)
    }

    @Test
    fun `list should show module as disabled when tenant has no entry`() {
        every { tenantRepository.findById(tenantId) } returns tenant
        every { moduleRepository.findAll() } returns listOf(lendingModule)
        every { tenantModuleRepository.findAllByTenantId(tenantId) } returns emptyList()

        val response = listUseCase.execute(tenantId)

        assertEquals(1, response.items.size)
        assertFalse(response.items.first().enabled)
    }
}