package br.com.deltaglobalbank.identity.features.users.createUser

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.Email
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.user.HashedPassword
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException
import br.com.deltaglobalbank.identity.domain.user.Password
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.domain.user.UserStatus
import br.com.deltaglobalbank.identity.features.users.createUser.CreateUserCommand
import br.com.deltaglobalbank.identity.features.users.createUser.CreateUserUseCase
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authorization.AuthorizationDeniedException
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class CreateUserTests {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var tenantRepository: TenantRepository

    @MockK
    lateinit var roleRepository: RoleRepository

    @MockK(relaxed = true)
    lateinit var tenantModuleRepository: TenantModuleRepository

    @MockK(relaxed = true)
    lateinit var moduleRepository: ModuleRepository

    @MockK
    lateinit var userRoleRepository: JpaUserRoleRepository

    @MockK(relaxed = true)
    lateinit var passwordHasher: SpringPasswordHasher

    @MockK(relaxed = true)
    lateinit var temporaryPasswordGenerator: TemporaryPasswordGenerator

    private lateinit var useCase: CreateUserUseCase

    @BeforeEach
    fun setUp() {
        useCase = CreateUserUseCase(
            userRepository,
            tenantRepository,
            roleRepository,
            tenantModuleRepository,
            moduleRepository,
            userRoleRepository,
            passwordHasher,
            temporaryPasswordGenerator
        )
    }

    @Test
    fun `must throw TenantNotFoundException when tenant does not exist`() {

        val tenantId = UUID.randomUUID()
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(RoleCode("platform.admin")),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        every { tenantRepository.findById(tenantId) } returns null

        assertThrows(TenantNotFoundException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must throw TenantInactiveException when tenant is inactive`() {
        val tenantId = UUID.randomUUID()
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(RoleCode("platform.admin")),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns false
        every { tenantRepository.findById(tenantId) } returns tenant

        assertThrows(TenantInactiveException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must throw EmailAlredyExistsException when email already exists`() {
        val tenantId = UUID.randomUUID()
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(RoleCode("platform.admin")),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns true

        assertThrows(EmailAlreadyExistsException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must throw DuplicateRoleException when role codes list has duplicates`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("identity.admin")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(roleCode, roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        assertThrows(DuplicateRoleException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must throw DuplicateRoleException when role codes differ only by case`() {
        val tenantId = UUID.randomUUID()
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(
                RoleCode("IDENTITY.ADMIN"),
                RoleCode("identity.admin")
            ),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        assertThrows(DuplicateRoleException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must throw RoleNotFoundException when role does not exist`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("nonexistent.role")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        every { roleRepository.findByCode(roleCode) } returns null

        assertThrows(RoleNotFoundException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }

    }

    @Test
    fun `must throw ModuleNotEnabledForTenantException when role module is not enabled`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("identity.viewer")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

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
        every { moduleRepository.findById(any()) } returns null

        assertThrows(ModuleNotEnabledForTenantException::class.java) {
            useCase.execute(command)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }

    }

    @Test
    fun `must create user with no roles when list is empty`() {
        val tenantId = UUID.randomUUID()
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = emptyList(),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        val rawPassword = "Password123!"
        val password = Password(rawPassword)
        every { temporaryPasswordGenerator.generatePassword() } returns rawPassword
        every { passwordHasher.hash(password) } returns HashedPassword("algumHashValido")
        every { userRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertEquals("admin@delta.com", response.user.email)
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }

    }

    @Test
    fun `must create user successfully with active status and temporary password requiring change`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("identity.admin")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

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

        val rawPassword = "Password123!"
        val password = Password(rawPassword)
        every { temporaryPasswordGenerator.generatePassword() } returns rawPassword
        every { passwordHasher.hash(password) } returns HashedPassword("algumHashValido")

        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns mockk(relaxed = true)
        every { userRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        val saved = userSlot.captured.snapshot()
        assertAll(
            { assertEquals(true, response.user.mustChangePassword) },
            { assertEquals("admin@delta.com", response.user.email) },
            { assertEquals(tenantId, response.user.tenantId) },
            { assertNotNull(response.temporaryPassword) },
            { assertTrue(userSlot.captured.isActive()) },
            { assertEquals(HashedPassword("algumHashValido"), saved.passwordHash) }
        )

        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must create platform-admin user when creator is platform-admin`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("platform.admin")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns null
        every { role.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(roleCode) } returns role

        val rawPassword = "Password123!"
        val password = Password(rawPassword)
        every { temporaryPasswordGenerator.generatePassword() } returns rawPassword
        every { passwordHasher.hash(password) } returns HashedPassword("algumHashValido")

        every { userRepository.save(any()) } returns mockk(relaxed = true)
        every { userRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertEquals("admin@delta.com", response.user.email)
        assertEquals(tenantId, response.user.tenantId)
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userRoleRepository.save(any()) }

    }

    @Test
    fun `must not create platform-admin user when creator is not platform-admin`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("platform.admin")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns null
        every { roleRepository.findByCode(roleCode) } returns role

        val ex = assertThrows(AuthorizationDeniedException::class.java) {
            useCase.execute(command)
        }
        assertEquals("denied", ex.message)

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must create user when creator is identity-admin`() {
        val tenantId = UUID.randomUUID()
        val roleCode = RoleCode("customers.viewer")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "customers.viewer@delta.com",
            roleCodes = listOf(roleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("customers.viewer@delta.com")
        every { userRepository.existsByEmail(email) } returns false

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

        val rawPassword = "Password123!"
        val password = Password(rawPassword)
        every { temporaryPasswordGenerator.generatePassword() } returns rawPassword
        every { passwordHasher.hash(password) } returns HashedPassword("algumHashValido")

        every { userRepository.save(any()) } returns mockk(relaxed = true)
        every { userRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertEquals("customers.viewer@delta.com", response.user.email)
        assertEquals(tenantId, response.user.tenantId)
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must not create platform-admin even when it is not first in the role list`() {
        val tenantId = UUID.randomUUID()
        val viewerCode = RoleCode("customers.viewer")
        val adminCode = RoleCode("platform.admin")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(viewerCode, adminCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("identity.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        val viewerRole = mockk<Role>()
        every { viewerRole.code } returns viewerCode
        every { viewerRole.moduleId } returns null
        every { roleRepository.findByCode(viewerCode) } returns viewerRole

        val adminRole = mockk<Role>()
        every { adminRole.code } returns adminCode
        every { adminRole.moduleId } returns null
        every { roleRepository.findByCode(adminCode) } returns adminRole

        val ex = assertThrows(AuthorizationDeniedException::class.java) {
            useCase.execute(command)
        }
        assertEquals("denied", ex.message)

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userRoleRepository.save(any()) }

    }

    @Test
    fun `must save one user-role entity per role when creating user with multiple roles`() {
        val tenantId = UUID.randomUUID()
        val adminCode = RoleCode("custumer.admin")
        val viewerCode = RoleCode("custumer.viewer")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "costumer@delta.com",
            roleCodes = listOf(adminCode, viewerCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("costumer@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        val moduleId = UUID.randomUUID()
        val adminRole = mockk<Role>()
        every { adminRole.code } returns adminCode
        every { adminRole.moduleId } returns moduleId
        every { adminRole.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(adminCode) } returns adminRole

        val viewerRole = mockk<Role>()
        every { viewerRole.code } returns viewerCode
        every { viewerRole.moduleId } returns moduleId
        every { viewerRole.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(viewerCode) } returns viewerRole

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(
            tenantModule
        )

        val rawPassword = "Password123!"
        val password = Password(rawPassword)
        every { temporaryPasswordGenerator.generatePassword() } returns rawPassword
        every { passwordHasher.hash(password) } returns HashedPassword("algumHashValido")

        every { userRepository.save(any()) } returns mockk(relaxed = true)
        every { userRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute(command)

        assertEquals("costumer@delta.com", response.user.email)
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 2) { userRoleRepository.save(any()) }
    }

    @Test
    fun `must create user with roles from different enabled modules`() {
        val tenantId = UUID.randomUUID()
        val customersModuleCode = RoleCode("customers.admin")
        val lendingModuleCode = RoleCode("lending.operator")
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = "New User",
            email = "admin@delta.com",
            roleCodes = listOf(customersModuleCode, lendingModuleCode),
            grantedBy = UUID.randomUUID(),
            creatorRoles = listOf("platform.admin")
        )

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val email = Email("admin@delta.com")
        every { userRepository.existsByEmail(email) } returns false

        val customersModuleId = UUID.randomUUID()
        val lendingModuleId = UUID.randomUUID()
        val customersRole = mockk<Role>()
        every { customersRole.code } returns customersModuleCode
        every { customersRole.moduleId } returns customersModuleId
        every { customersRole.id } returns UUID.randomUUID()
        val lendingRole = mockk<Role>()
        every { lendingRole.code } returns lendingModuleCode
        every { lendingRole.moduleId } returns lendingModuleId
        every { lendingRole.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(customersModuleCode) } returns customersRole
        every { roleRepository.findByCode(lendingModuleCode) } returns lendingRole

        val customersModule = mockk<TenantModule>()
        every { customersModule.moduleId } returns customersModuleId
        val lendingModule = mockk<TenantModule>()
        every { lendingModule.moduleId } returns lendingModuleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(
            customersModule,
            lendingModule
        )

        val rawPassword = "Password123!"
        val password = Password(rawPassword)
        every { temporaryPasswordGenerator.generatePassword() } returns rawPassword
        every { passwordHasher.hash(password) } returns HashedPassword("algumHashValido")
        every { userRepository.save(any()) } returns mockk(relaxed = true)
        every { userRoleRepository.save(any()) } returns mockk(relaxed = true)

        useCase.execute(command)

        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 2) { userRoleRepository.save(any()) }
    }

}