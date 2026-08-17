package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeyUseCase

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyRotator
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SigningKeyRotatorTests {

    @MockK
    lateinit var signingKeyRepository: SigningKeyRepository

    @MockK
    lateinit var keyGenerator: KeyGenerator

    private lateinit var rotator: SigningKeyRotator

    @BeforeEach
    fun setUp() {
        rotator = SigningKeyRotator(signingKeyRepository, keyGenerator)

        val fakePair = mockk<java.security.KeyPair>(relaxed = true)
        every { keyGenerator.generateRsaKeyPair() } returns fakePair
        every { keyGenerator.encodePublicKey(any()) } returns "pub-new"
        every { keyGenerator.encodePrivateKey(any()) } returns "priv-new"

        every { signingKeyRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `must retire the previous key and create a new active key`() {
        val previous = SigningKey.create("key-old", "RS256", "pub-old", "priv-old")
        every { signingKeyRepository.findFirstActive() } returns previous

        val (newKey, prev) = rotator.rotate()

        assertAll(
            { assertEquals(SigningKeyStatus.RETIRED, prev!!.status()) },
            { assertEquals(SigningKeyStatus.ACTIVE, newKey.status()) },
            { assertNotEquals(prev!!.kid, newKey.kid) }
        )

        verify(exactly = 2) { signingKeyRepository.save(any()) }
    }

    @Test
    fun `must create a new active key without retiring when there is no previous key`() {
        every { signingKeyRepository.findFirstActive() } returns null

        val (newKey, prev) = rotator.rotate()

        assertAll(
            { assertNull(prev) },
            { assertEquals(SigningKeyStatus.ACTIVE, newKey.status()) }
        )

        verify(exactly = 1) { signingKeyRepository.save(any()) }
    }
}