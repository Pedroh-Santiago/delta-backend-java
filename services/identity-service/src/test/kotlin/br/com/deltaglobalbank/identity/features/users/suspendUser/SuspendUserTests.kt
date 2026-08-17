package br.com.deltaglobalbank.identity.features.users.suspendUser

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.verify
import java.util.UUID

@ExtendWith(MockKExtension::class)
class SuspendUserTests {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var refreshTokenRevoker: RefreshTokenRevoker

    @MockK
    lateinit var accessTokenRevoker: AccessTokenRevoker

    lateinit var useCase: SuspendUserUseCase

    @BeforeEach
    fun setUp() { useCase = SuspendUserUseCase(refreshTokenRevoker, userRepository, accessTokenRevoker ) }

    @Test
    fun `suspends user and revokes access and refresh tokens`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val user = mockk<User>(relaxed = true)
        every { user.tenantId } returns tenantId
        every { userRepository.findById(userId) } returns user
        every { userRepository.save(user) } returns user
        every { accessTokenRevoker.revokeUser(userId) } just Runs
        every { refreshTokenRevoker.revokeAllForUser(userId) } just Runs

        useCase.suspendUser(userId, tenantId, UUID.randomUUID())

        verify { user.suspend() }
        verify { userRepository.save(user) }
        verify { accessTokenRevoker.revokeUser(userId) }
        verify { refreshTokenRevoker.revokeAllForUser(userId) }
    }
}