package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeyUseCase

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyRotator
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.RotateSigningKeyUseCase
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import io.mockk.just
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class RotateSigningKeyUseCaseTests {

    @MockK
    lateinit var rotation: SigningKeyRotator
    @MockK
    lateinit var keyManager: KeyManager

    private lateinit var useCase: RotateSigningKeyUseCase

    @BeforeEach
    fun setUp() {
        useCase = RotateSigningKeyUseCase(
            rotation,
            keyManager
        )
    }

    @Test
    fun `must rotate key and refresh key manager with previous key`() {
        val rotatedBy = UUID.randomUUID()

        val newKey = SigningKey.create(
            kid = "key-new",
            algorithm = "RS256",
            publicKey = "pub",
            privateKey = "priv"
        )
        val previousKey = SigningKey.create(
            kid = "key-old",
            algorithm = "RS256",
            publicKey = "pub-old",
            privateKey = "priv-old"
        )
        every { rotation.rotate() } returns (newKey to previousKey)
        every { keyManager.refresh() } just Runs

        val response = useCase.execute(rotatedBy)

        assertAll(
            { assertNotNull(response.newKey) },
            { assertNotNull(response.previousKey) },
            { assertEquals("key-new", response.newKey.kid) }
        )
        verify(exactly = 1) { rotation.rotate() }
        verify(exactly = 1) { keyManager.refresh() }
    }

    @Test
    fun `must rotate key with null previous key on first rotation`() {
        val rotatedBy = UUID.randomUUID()

        val newKey = SigningKey.create(
            kid = "key-new",
            algorithm = "RS256",
            publicKey = "pub",
            privateKey = "priv"
        )
        every { rotation.rotate() } returns (newKey to null)
        every { keyManager.refresh() } just Runs

        val response = useCase.execute(rotatedBy)

        assertAll(
            { assertNotNull(response.newKey) },
            { assertEquals("key-new", response.newKey.kid) },
            { assertNull(response.previousKey) }
        )
        verify(exactly = 1) { rotation.rotate() }
        verify(exactly = 1) { keyManager.refresh() }
    }

}