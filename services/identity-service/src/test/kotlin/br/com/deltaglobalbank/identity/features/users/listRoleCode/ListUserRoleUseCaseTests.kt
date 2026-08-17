package br.com.deltaglobalbank.identity.features.users.listRoleCode

import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.Module
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.features.users.listUserRoles.ListUserRoleQuery
import br.com.deltaglobalbank.identity.features.users.listUserRoles.ListUserRoleUseCase
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class ListUserRoleUseCaseTests {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var userRoleRepository: JpaUserRoleRepository

    @MockK
    lateinit var roleRepository: RoleRepository

    @MockK
    lateinit var moduleRepository: ModuleRepository

    private lateinit var useCase: ListUserRoleUseCase

    private lateinit var userId: UUID
    private lateinit var tenantId: UUID
    private lateinit var roleId: UUID
    private lateinit var moduleId: UUID

    @BeforeEach
    fun setUp() {
        useCase = ListUserRoleUseCase(
            userRepository,
            userRoleRepository,
            roleRepository,
            moduleRepository
        )
        userId = UUID.randomUUID()
        tenantId = UUID.randomUUID()
        roleId = UUID.randomUUID()
        moduleId = UUID.randomUUID()
    }

    @Test
    fun `must return empty list when user has no roles`() {
        val query = ListUserRoleQuery(userId = userId, tenantId = tenantId)

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        every { userRoleRepository.findAllByUserId(userId) } returns emptyList()

        val response = useCase.execute(query)

        assertTrue(response.items.isEmpty())

        verify(exactly = 0) { roleRepository.findAllByIds(any()) }
        verify(exactly = 0) { moduleRepository.findAllByIds(any()) }
    }
    @Test
    fun `must assemble role with its module code`() {
        val query = ListUserRoleQuery(userId = userId, tenantId = tenantId)
        val grantedAtInstant = Instant.now()
        val grantedByUserId = UUID.randomUUID()

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val entry = mockk<UserRoleEntity>()
        every { entry.roleId } returns roleId
        every { entry.grantedAt } returns grantedAtInstant
        every { entry.grantedBy } returns grantedByUserId
        every { userRoleRepository.findAllByUserId(userId) } returns listOf(entry)

        val role = mockk<Role>()
        every { role.id } returns roleId
        every { role.code } returns RoleCode("customers.admin")
        every { role.moduleId } returns moduleId
        every { roleRepository.findAllByIds(setOf(roleId)) } returns listOf(role)

        val module = mockk<Module>()
        every { module.id } returns moduleId
        every { module.code } returns ModuleCode("customers")
        every { moduleRepository.findAllByIds(setOf(moduleId)) } returns listOf(module)

        val response = useCase.execute(query)

        val item = response.items.single()
        assertAll(
            { assertEquals("customers.admin", item.roleCode) },
            { assertEquals(roleId, item.roleId) },
            { assertEquals("customers", item.moduleCode!!) },
            { assertEquals(grantedAtInstant, item.grantedAt) },
            { assertEquals(grantedByUserId, item.grantedBy) }
        )
    }

    @Test
    fun `must list role with null module code when role has no module`() {
        val query = ListUserRoleQuery(userId = userId, tenantId = tenantId)
        val grantedByUserId = UUID.randomUUID()

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val entry = mockk<UserRoleEntity>()
        every { entry.roleId } returns roleId
        every { entry.grantedAt } returns Instant.now()
        every { entry.grantedBy } returns grantedByUserId
        every { userRoleRepository.findAllByUserId(userId) } returns listOf(entry)

        val role = mockk<Role>()
        every { role.id } returns roleId
        every { role.code } returns RoleCode("platform.admin")
        every { role.moduleId } returns null
        every { roleRepository.findAllByIds(setOf(roleId)) } returns listOf(role)

        every { moduleRepository.findAllByIds(emptySet()) } returns emptyList()

        val response = useCase.execute(query)

        val item = response.items.single()
        assertAll(
            { assertEquals("platform.admin", item.roleCode) },
            { assertNull(item.moduleCode) }
        )
    }

    @Test
    fun `must assemble multiple roles with and without modules`() {
        val query = ListUserRoleQuery(userId = userId, tenantId = tenantId)

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user

        val roleAId = UUID.randomUUID()
        val moduleAId = UUID.randomUUID()
        val entryA = mockk<UserRoleEntity>()
        every { entryA.roleId } returns roleAId
        every { entryA.grantedAt } returns Instant.now()
        every { entryA.grantedBy } returns UUID.randomUUID()

        val roleBId = UUID.randomUUID()
        val entryB = mockk<UserRoleEntity>()
        every { entryB.roleId } returns roleBId
        every { entryB.grantedAt } returns Instant.now()
        every { entryB.grantedBy } returns UUID.randomUUID()

        every { userRoleRepository.findAllByUserId(userId) } returns listOf(entryA, entryB)

        val roleA = mockk<Role>()
        every { roleA.id } returns roleAId
        every { roleA.code } returns RoleCode("customers.admin")
        every { roleA.moduleId } returns moduleAId
        val roleB = mockk<Role>()
        every { roleB.id } returns roleBId
        every { roleB.code } returns RoleCode("platform.admin")
        every { roleB.moduleId } returns null
        every { roleRepository.findAllByIds(setOf(roleAId, roleBId)) } returns listOf(roleA, roleB)

        val moduleA = mockk<Module>()
        every { moduleA.id } returns moduleAId
        every { moduleA.code } returns ModuleCode("customers")
        every { moduleRepository.findAllByIds(setOf(moduleAId)) } returns listOf(moduleA)

        val response = useCase.execute(query)

        val itemA = response.items.single { it.roleId == roleAId }
        val itemB = response.items.single { it.roleId == roleBId }
        assertAll(
            { assertEquals(2, response.items.size) },
            { assertEquals("customers.admin", itemA.roleCode) },
            { assertEquals("customers", itemA.moduleCode!!) },
            { assertEquals("platform.admin", itemB.roleCode) },
            { assertNull(itemB.moduleCode) }
        )
    }

    @Test
    fun `must throw UserNotFound when user belongs to a different tenant`() {
        val query = ListUserRoleQuery(userId = userId, tenantId = UUID.randomUUID())
        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { userRepository.findById(userId) } returns user
        assertThrows(UserNotFound::class.java) { useCase.execute(query) }
    }

}