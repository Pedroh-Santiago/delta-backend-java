package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeyUseCase

import br.com.deltaglobalbank.identity.domain.token.CannotRevokeActiveKeyException
import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyNotFoundException
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys.SigningKeyRevoker
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SigningKeyRevokerTests {
    @MockK lateinit var signingKeyRepository: SigningKeyRepository
    private lateinit var revoker: SigningKeyRevoker

    @BeforeEach fun setUp() { revoker = SigningKeyRevoker(signingKeyRepository) }

    @Test fun `must throw 404 when key does not exist`() {
        val id = UUID.randomUUID()
        every { signingKeyRepository.findById(id) } returns null
        assertThrows<SigningKeyNotFoundException> { revoker.revoke(id) }
        verify(exactly = 0) { signingKeyRepository.save(any()) }
    }

    @Test fun `must throw 409 when key is active`() {
        val active = SigningKey.create("key-active","RS256","pub","priv")  // nasce ACTIVE
        every { signingKeyRepository.findById(any()) } returns active
        assertThrows<CannotRevokeActiveKeyException> { revoker.revoke(active.id) }
        verify(exactly = 0) { signingKeyRepository.save(any()) }
    }

    @Test fun `must revoke a retired key`() {
        val retired = SigningKey.create("key-ret","RS256","pub","priv").apply { retire() }
        every { signingKeyRepository.findById(any()) } returns retired
        every { signingKeyRepository.save(any()) } answers { firstArg() }

        val mutated = revoker.revoke(retired.id)

        assertTrue(mutated)
        assertEquals(SigningKeyStatus.REVOKED, retired.status())
        verify(exactly = 1) { signingKeyRepository.save(any()) }
    }

    @Test fun `must be idempotent when key already revoked`() {
        val revoked = SigningKey.create("key-rev","RS256","pub","priv").apply { revoke() }
        every { signingKeyRepository.findById(any()) } returns revoked
        assertFalse(revoker.revoke(revoked.id))
        verify(exactly = 0) { signingKeyRepository.save(any()) }
    }
}