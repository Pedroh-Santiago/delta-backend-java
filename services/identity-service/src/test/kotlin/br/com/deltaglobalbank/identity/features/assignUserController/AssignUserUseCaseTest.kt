package br.com.deltaglobalbank.identity.features.assignUserController

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.TenantAccessDeniedException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.features.assignUserRole.AssignRoleUseCase
import br.com.deltaglobalbank.identity.features.assignUserRole.AssignRolesRequest
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authorization.AuthorizationDeniedException
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AssignUserUseCaseTest {

    @MockK
    lateinit var tenantModuleRepository : TenantModuleRepository

    @MockK
    lateinit var moduleRepository: ModuleRepository

    @MockK
    lateinit var userRepository : UserRepository

    @MockK
    lateinit var roleRepository : RoleRepository

    @MockK
    lateinit var jpaUserRoleRepository: JpaUserRoleRepository

    lateinit var  assignUserUseCase : AssignRoleUseCase

    @BeforeEach
    fun setup(){
        assignUserUseCase = AssignRoleUseCase(
            tenantModuleRepository,
            moduleRepository,
            userRepository,
            roleRepository,
            jpaUserRoleRepository
        )
    }

    @Test
    fun `should throw UserNotFound when user not found`() {
        val userId = UUID.randomUUID()
        val actingTenantId = UUID.randomUUID()
        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = actingTenantId,
            principalType = "user", roles = listOf("identity.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )
        val request = AssignRolesRequest(rolesCodes = listOf(RoleCode("lending.viewer")))

        every { userRepository.findById(userId) }.returns(null)

        assertThrows<UserNotFound> {
            assignUserUseCase.assignRoleTenant(actingTenantId, principal, userId, request)
        }

        verify(exactly = 0) { jpaUserRoleRepository.save(any()) }
    }

    @Test
    fun `should throw TenantAccessDeniedException when user belongs to another tenant`() {
        val userId = UUID.randomUUID()
        val user = mockk<User>()
        val otherTenantId = UUID.randomUUID()
        val actingTenantId = UUID.randomUUID()
        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = actingTenantId,
            principalType = "user", roles = listOf("identity.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )
        val request = AssignRolesRequest(rolesCodes = listOf(RoleCode("lending.viewer")))

        every { user.tenantId } returns otherTenantId
        every { userRepository.findById(userId) } returns user

        assertThrows<UserNotFound> {
            assignUserUseCase.assignRoleTenant(actingTenantId, principal, userId, request)
        }

        verify(exactly = 0) { jpaUserRoleRepository.save(any()) }
    }

    @Test
    fun `should throw RoleNotFoundException when role code does not exist`(){
        val userId = UUID.randomUUID()
        val user = mockk<User>()
        val actingTenantId = UUID.randomUUID()
        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = actingTenantId,
            principalType = "user", roles = listOf("identity.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )
        val roleCode = RoleCode("lending.viewer")
        val request = AssignRolesRequest(rolesCodes = listOf(roleCode))

        every { roleRepository.findByCode(roleCode) } returns null
        every { user.tenantId } returns actingTenantId
        every { userRepository.findById(userId) } returns user


       assertThrows<RoleNotFoundException>{
           assignUserUseCase.assignRoleTenant(actingTenantId, principal, userId, request)
       }

        verify(exactly = 0) { jpaUserRoleRepository.save(any()) }
    }

    @Test
    fun `should throw AuthorizationDeniedException when non platform-admin grants platform-admin`(){
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val actingTenantId = UUID.randomUUID()
        val user = mockk<User>()
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = actingTenantId,
            principalType = "user", roles = listOf("identity.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )

        val roleCode = RoleCode("platform.admin")
        val request = AssignRolesRequest(rolesCodes = listOf(roleCode))

        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns null
        every { roleRepository.findByCode(roleCode) } returns role

        val ex = assertThrows<AuthorizationDeniedException>{
            assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request)
        }

        assertEquals("denied", ex.message)

        verify(exactly = 0) { jpaUserRoleRepository.save(any())}

    }

    @Test
    fun `should assign new role and return added roles when all validations pass`(){
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val user = mockk<User>()
        val userIdValue = UUID.randomUUID()
        every { user.id } returns userIdValue
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = tenantId,
            principalType = "user", roles = listOf("platform.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )

        val moduleId = UUID.randomUUID()
        val roleCode = RoleCode("lending.viewer")
        val request = AssignRolesRequest(rolesCodes = listOf(roleCode))

        val role = mockk<Role>()
        every { role.code } returns roleCode
        every { role.moduleId } returns moduleId
        every { role.id } returns UUID.randomUUID()
        every { roleRepository.findByCode(roleCode) } returns role

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(tenantModule)

        every { roleRepository.findAllByUserId(userIdValue) } returns emptyList()

        every { jpaUserRoleRepository.save(any()) } returns mockk(relaxed = true)

        val response = assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request)

        verify(exactly = 1) { jpaUserRoleRepository.save(any()) }
        assertEquals(userIdValue, response.userId)
        assertEquals(listOf(roleCode), response.addedRoles)
    }

    @Test
    fun `should not save when user already has the requested role`(){
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val user = mockk<User>()
        val userIdValue = UUID.randomUUID()
        val moduleId = UUID.randomUUID()
        val roleCode = RoleCode("lending.viewer")
        val existingRole = Role(
            id = UUID.randomUUID(),
            code = roleCode,
            moduleId = moduleId,
            description = null,
            createdAt = Instant.now()
        )

        every { user.id } returns userIdValue
        every { roleRepository.findByCode(roleCode) } returns existingRole
        every { roleRepository.findAllByUserId(userIdValue) } returns listOf(existingRole)
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(tenantModule)

        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = tenantId,
            principalType = "user", roles = listOf("platform.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )

        val request = AssignRolesRequest(rolesCodes = listOf(RoleCode("lending.viewer")))

        val response = assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request)

        verify(exactly = 0) { jpaUserRoleRepository.save(any()) }
        assertEquals(emptyList(), response.addedRoles)
    }

    @Test
    fun `should add only the missing role when user already has one of the requested roles`(){
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val userIdValue = UUID.randomUUID()
        val moduleId = UUID.randomUUID()
        val user = mockk<User>()

        val existingRole = Role(
            id = UUID.randomUUID(), code = RoleCode("lending.viewer"),
            moduleId = moduleId, description = null, createdAt = Instant.now()
        )
        val newRole = Role(
            id = UUID.randomUUID(), code = RoleCode("customers.viewer"),
            moduleId = moduleId, description = null, createdAt = Instant.now()
        )

        val request = AssignRolesRequest(rolesCodes = listOf(existingRole.code, newRole.code))

        every { user.id } returns userIdValue
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        every { roleRepository.findByCode(existingRole.code) } returns existingRole
        every { roleRepository.findByCode(newRole.code) } returns newRole

        every { roleRepository.findAllByUserId(userIdValue) } returns listOf(existingRole)

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(tenantModule)

        every { jpaUserRoleRepository.save(any()) } returns mockk(relaxed = true)

        val principal = AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = tenantId,
            principalType = "user", roles = listOf("platform.admin"),
            modules = emptyList(), mustChangePassword = false, jti = UUID.randomUUID()
        )

        val response = assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request)

        verify(exactly = 1) { jpaUserRoleRepository.save(any()) }
        assertEquals(listOf(newRole.code), response.addedRoles)
    }
}