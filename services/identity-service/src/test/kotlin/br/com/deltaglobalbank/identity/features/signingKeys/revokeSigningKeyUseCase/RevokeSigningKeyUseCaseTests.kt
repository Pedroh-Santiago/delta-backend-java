package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeyUseCase

import br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys.RevokeSigningKeyUseCase
import br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys.SigningKeyRevoker
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class RevokeSigningKeyUseCaseTests {

    @MockK
    lateinit var revoker: SigningKeyRevoker
    @MockK
    lateinit var keyManager: KeyManager

    private lateinit var useCase: RevokeSigningKeyUseCase

    @BeforeEach
    fun setUp() {
        useCase = RevokeSigningKeyUseCase(
            revoker,
            keyManager
        )
    }

    @Test
    fun `must refresh key manager when revocation mutated a key`() {
        val keyId = UUID.randomUUID()
        val revokedBy = UUID.randomUUID()

        every { revoker.revoke(keyId) } returns true
        every { keyManager.refresh() } just Runs

        useCase.execute(keyId, revokedBy)

        verify(exactly = 1) { keyManager.refresh() }
    }

    @Test
    fun `must not refresh key manager when revocation did not mutate`() {
        val keyId = UUID.randomUUID()
        val revokedBy = UUID.randomUUID()

        every { revoker.revoke(keyId) } returns false

        useCase.execute(keyId, revokedBy)

        verify(exactly = 0) { keyManager.refresh() }
    }
}