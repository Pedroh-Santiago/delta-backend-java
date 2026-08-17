package br.com.deltaglobalbank.identity.features.users.removeRoleCode

import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.user.CannotRemoveOwnAdminRoleException
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.features.users.removeUserRole.RemoveUserRoleCommand
import br.com.deltaglobalbank.identity.features.users.removeUserRole.RemoveUserRoleUseCase
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class RemoveUserRoleUseCaseTests {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var roleRepository: RoleRepository

    @MockK
    lateinit var userRoleRepository: JpaUserRoleRepository

    private lateinit var useCase: RemoveUserRoleUseCase

    @BeforeEach
    fun setUp() {
        useCase = RemoveUserRoleUseCase(
            userRepository,
            roleRepository,
            userRoleRepository
        )
    }
    @Test
    fun `must throw when removing own admin role`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val principal = mockk<AuthenticatedPrincipal>()
        every { principal.subject } returns userId

        val command = RemoveUserRoleCommand(
            userId = userId,
            tenantId = tenantId,
            roleCode = RoleCode("platform.admin"),
            principal = principal
        )
        val user = mockk<User>()
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        Assertions.assertThrows(CannotRemoveOwnAdminRoleException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { userRoleRepository.delete(any()) }
    }

    @Test
    fun `must allow removing own non-admin role`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val principal = mockk<AuthenticatedPrincipal>()
        every { principal.subject } returns userId
        val command = RemoveUserRoleCommand(
            userId = userId,
            tenantId = tenantId,
            roleCode = RoleCode("customers.viewer"),
            principal = principal
        )

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val role = mockk<Role>()
        every { role.id } returns roleId
        every { roleRepository.findByCode(RoleCode("customers.viewer")) } returns role

        val entry = mockk<UserRoleEntity>()
        every { userRoleRepository.findByUserIdAndRoleId(userId, roleId) } returns entry
        every { userRoleRepository.delete(entry) } just Runs

        useCase.execute(command)

        verify(exactly = 1) { userRoleRepository.delete(entry) }
    }

    @Test
    fun `must allow removing admin role from another user`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val principal = mockk<AuthenticatedPrincipal>()
        every { principal.subject } returns UUID.randomUUID()

        val command = RemoveUserRoleCommand(
            userId = userId,
            tenantId = tenantId,
            roleCode = RoleCode("platform.admin"),
            principal = principal
        )

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val role = mockk<Role>()
        every { role.id } returns roleId
        every { roleRepository.findByCode(RoleCode("platform.admin")) } returns role

        val entry = mockk<UserRoleEntity>()
        every { userRoleRepository.findByUserIdAndRoleId(userId, roleId) } returns entry
        every { userRoleRepository.delete(entry) } just Runs

        useCase.execute(command)

        verify(exactly = 1) { userRoleRepository.delete(entry) }
    }

    @Test
    fun `must do nothing when user does not have the role`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val principal = mockk<AuthenticatedPrincipal>()
        every { principal.subject } returns UUID.randomUUID()

        val command = RemoveUserRoleCommand(
            userId = userId,
            tenantId = tenantId,
            roleCode = RoleCode("customers.viewer"),
            principal = principal
        )

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val role = mockk<Role>()
        every { role.id } returns roleId
        every { roleRepository.findByCode(RoleCode("customers.viewer")) } returns role

        every { userRoleRepository.findByUserIdAndRoleId(userId, roleId) } returns null

        useCase.execute(command)

        verify(exactly = 0) { userRoleRepository.delete(any()) }
    }

    @Test
    fun `must throw UserNotFound when user does not exist`() {
        val principal = mockk<AuthenticatedPrincipal>()
        val command = RemoveUserRoleCommand(
            userId = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            roleCode = RoleCode("customers.viewer"),
            principal = principal
        )
        every { userRepository.findById(command.userId) } returns null   // portão 1

        Assertions.assertThrows(UserNotFound::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { userRoleRepository.delete(any()) }
    }

    @Test
    fun `must throw UserNotFound when user belongs to a different tenant`() {
        val userId = UUID.randomUUID()
        val ownerTenantId = UUID.randomUUID()
        val attackerTenantId = UUID.randomUUID()
        val principal = mockk<AuthenticatedPrincipal>()
        val command = RemoveUserRoleCommand(
            userId = userId,
            tenantId = attackerTenantId,
            roleCode = RoleCode("customers.viewer"),
            principal = principal
        )

        val user = mockk<User>()
        every { user.tenantId } returns ownerTenantId
        every { userRepository.findById(userId) } returns user

        Assertions.assertThrows(UserNotFound::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { userRoleRepository.delete(any()) }
    }

    @Test
    fun `must throw RoleNotFoundException when role does not exist`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val principal = mockk<AuthenticatedPrincipal>()
        every { principal.subject } returns UUID.randomUUID()

        val command = RemoveUserRoleCommand(
            userId = userId,
            tenantId = tenantId,
            roleCode = RoleCode("nonexistent.role"),
            principal = principal
        )

        val user = mockk<User>()
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        every { roleRepository.findByCode(RoleCode("nonexistent.role")) } returns null

        Assertions.assertThrows(RoleNotFoundException::class.java) {
            useCase.execute(command)
        }
        verify(exactly = 0) { userRoleRepository.delete(any()) }
    }

}