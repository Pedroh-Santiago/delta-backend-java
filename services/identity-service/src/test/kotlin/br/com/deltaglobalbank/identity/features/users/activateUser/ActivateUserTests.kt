package br.com.deltaglobalbank.identity.features.users.activateUser

import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.verify
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ActivateUserTests {


    @MockK
    lateinit var userRepository: UserRepository

    lateinit var useCase: ActivateUserUseCase

    @BeforeEach
    fun setup() { useCase = ActivateUserUseCase(userRepository) }

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> =
        GenericContainer(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379)


    @Test
    fun `activates user when in same tenant`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val user = mockk<User>(relaxed = true)
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user
        every { userRepository.save(user) } returns user

        useCase.activateUser(userId, tenantId, UUID.randomUUID())

        verify { user.activate() }
        verify { userRepository.save(user) }
    }

    @Test
    fun `throws when user not found`() {
        every { userRepository.findById(any()) } returns null
        assertThrows(UserNotFound::class.java) {
            useCase.activateUser(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        }
    }

    @Test
    fun `throws when user belongs to another tenant`() {
        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { userRepository.findById(any()) } returns user
        assertThrows(UserNotFound::class.java) {
            useCase.activateUser(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        }
    }
}
