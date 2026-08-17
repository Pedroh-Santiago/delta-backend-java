package br.com.deltaglobalbank.identity.features.apiClient.createApiClient

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.features.apiClients.createApiClient.CreateApiClientCommand
import br.com.deltaglobalbank.identity.features.apiClients.createApiClient.CreateApiClientUseCase
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authorization.AuthorizationDeniedException
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CreateApiClientTests {

    @MockK
    lateinit var apiClientRepository: ApiClientRepository

    @MockK
    lateinit var tenantRepository: TenantRepository

    @MockK
    lateinit var roleRepository: RoleRepository

    @MockK(relaxed = true)
    lateinit var tenantModuleRepository: TenantModuleRepository

    @MockK(relaxed = true)
    lateinit var moduleRepository: ModuleRepository

    @MockK
    lateinit var apiClientRoleRepository: JpaApiClientRoleRepository

    private lateinit var useCase: CreateApiClientUseCase

    @BeforeEach
    fun setUp() {
        useCase = CreateApiClientUseCase(
            apiClientRepository,
            tenantRepository,
            roleRepository,
            tenantModuleRepository,
            moduleRepository,
            apiClientRoleRepository
        )
    }

    @Test
    fun `must create api client successfully with roles`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("customers.admin")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = "ERP",
            roleCodes = listOf(roleCode),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val moduleId = UUID.randomUUID()
        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns moduleId
        every { role.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(roleCode) } returns role

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(
            tenantModule
        )

        every { apiClientRepository.save(any()) } returns mockk(relaxed = true)
        every { apiClientRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertAll(
            { assertEquals(tenantId, response.tenantId) },
            { assertEquals("ERP-client", response.name) },
            { assertEquals("ERP", response.description) }
        )

        verify(exactly = 1) { apiClientRepository.save(any()) }
        verify(exactly = 1) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must throw TenantNotFoundException when tenant does not exist`() {
        val command = CreateApiClientCommand(
            tenantId = UUID.randomUUID(),
            name = "ERP-client",
            description = null,
            roleCodes = listOf(RoleCode("customers.admin")),
            creatorRoles = listOf("platform.admin")
        )

        every { tenantRepository.findById(any()) } returns null

        assertThrows(TenantNotFoundException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must throw TenantInactiveException when tenant is inactive`() {
        val tenantId = UUID.randomUUID()
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(RoleCode("customers.admin")),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns false
        every { tenantRepository.findById(tenantId) } returns tenant

        assertThrows(TenantInactiveException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must throw DuplicateRoleException when role codes are exactly duplicated`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("customers.admin")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(roleCode, roleCode),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        assertThrows(DuplicateRoleException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must throw DuplicateRoleException when role codes differ only by case`() {
        val tenantId = UUID.randomUUID()
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(
                RoleCode("CUSTOMERS.ADMIN"),
                RoleCode("customers.admin")
            ),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        assertThrows(DuplicateRoleException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must throw RoleNotFoundException when role does not exist`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("nonexistent.role")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(roleCode),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        every { roleRepository.findByCode(roleCode) } returns null

        assertThrows(RoleNotFoundException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must throw ModuleNotEnabledForTenantException when role module is not enabled`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("customers.admin")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(roleCode),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val moduleId = UUID.randomUUID()
        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns moduleId
        every { roleRepository.findByCode(roleCode) } returns role

        every {
            tenantModuleRepository.findAllByTenantIdAndEnabled(
                tenantId,
                true
            )
        } returns emptyList()
        every { moduleRepository.findById(moduleId) } returns null

        assertThrows(ModuleNotEnabledForTenantException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must deny when non-platform-admin creates api client with platform-admin role`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("platform.admin")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(roleCode),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns null
        every { roleRepository.findByCode(roleCode) } returns role

        val ex = assertThrows(AuthorizationDeniedException::class.java) {
            useCase.execute(command)
        }
        assertEquals("denied", ex.message)

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must allow platform-admin to create api client with platform-admin role`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("platform.admin")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(roleCode),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns null
        every { role.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(roleCode) } returns role

        every { apiClientRepository.save(any()) } returns mockk(relaxed = true)
        every { apiClientRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertEquals(tenantId, response.tenantId)
        verify(exactly = 1) { apiClientRepository.save(any()) }
        verify(exactly = 1) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must deny platform-admin role even when not first in the list`() {
        val tenantId = UUID.randomUUID()
        val commonCode = RoleCode("customers.viewer")
        val adminCode = RoleCode("platform.admin")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(commonCode, adminCode),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val commonRole = mockk<Role>()
        every { commonRole.code } returns commonCode
        every { commonRole.moduleId } returns null
        every { roleRepository.findByCode(commonCode) } returns commonRole

        val adminRole = mockk<Role>()
        every { adminRole.code } returns adminCode
        every { adminRole.moduleId } returns null
        every { roleRepository.findByCode(adminCode) } returns adminRole

        val ex = assertThrows(AuthorizationDeniedException::class.java) {
            useCase.execute(command)
        }
        assertEquals("denied", ex.message)

        verify(exactly = 0) { apiClientRepository.save(any()) }
        verify(exactly = 0) { apiClientRoleRepository.save(any()) }
    }

    @Test
    fun `must allow non-platform-admin to create api client with common role`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("customers.viewer")
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = "ERP-client",
            description = null,
            roleCodes = listOf(roleCode),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val moduleId = UUID.randomUUID()
        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns moduleId
        every { role.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(roleCode) } returns role

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(
            tenantModule
        )

        every { apiClientRepository.save(any()) } returns mockk(relaxed = true)
        every { apiClientRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertEquals(tenantId, response.tenantId)
        verify(exactly = 1) { apiClientRepository.save(any()) }
        verify(exactly = 1) { apiClientRoleRepository.save(any()) }
    }

}