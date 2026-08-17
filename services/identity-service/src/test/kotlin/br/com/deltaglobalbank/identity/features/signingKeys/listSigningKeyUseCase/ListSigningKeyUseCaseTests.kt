package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeyUseCase

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys.ListSigningKeysUseCase
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class ListSigningKeyUseCaseTests {

    @MockK
    lateinit var signingKeyRepository: SigningKeyRepository

    private lateinit var useCase: ListSigningKeysUseCase

    @BeforeEach
    fun setUp() {
        useCase = ListSigningKeysUseCase(
            signingKeyRepository
        )
    }

    @Test
    fun `must return empty list when there are no signing keys`() {
        every { signingKeyRepository.findAllByStatus(any()) } returns emptyList()

        val response = useCase.execute()

        assertTrue(response.items.isEmpty())
    }

    @Test
    fun `must list signing keys from all statuses`() {
        val activeKey = SigningKey.create(kid = "key-active", algorithm = "RS256", publicKey = "pub", privateKey = "priv")
        val retiredKey = SigningKey.create(kid = "key-retired", algorithm = "RS256", publicKey = "pub2", privateKey = "priv2")
        retiredKey.retire()
        val revokedKey = SigningKey.create(kid = "key-revoked", algorithm = "RS256", publicKey = "pub3", privateKey = "priv3")
        revokedKey.revoke()

        every { signingKeyRepository.findAllByStatus(SigningKeyStatus.ACTIVE) } returns listOf(activeKey)
        every { signingKeyRepository.findAllByStatus(SigningKeyStatus.RETIRED) } returns listOf(retiredKey)
        every { signingKeyRepository.findAllByStatus(SigningKeyStatus.REVOKED) } returns listOf(revokedKey)

        val response = useCase.execute()

        assertAll(
            { assertEquals(3, response.items.size) },
            { assertTrue(response.items.any { it.kid == "key-active" }) },
            { assertTrue(response.items.any { it.kid == "key-retired" }) },
            { assertTrue(response.items.any { it.kid == "key-revoked" }) }
        )
    }
}