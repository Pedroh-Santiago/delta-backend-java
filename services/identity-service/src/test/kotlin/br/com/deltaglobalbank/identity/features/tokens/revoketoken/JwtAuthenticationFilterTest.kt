package br.com.deltaglobalbank.identity.features.tokens.revoketoken

import br.com.deltaglobalbank.sharedauth.JwtAuthenticationFilter
import br.com.deltaglobalbank.sharedauth.JwtValidator
import br.com.deltaglobalbank.sharedauth.RevocationReason
import br.com.deltaglobalbank.sharedauth.TokenRevocationChecker
import br.com.deltaglobalbank.sharedauth.ValidatedJwt
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class JwtAuthenticationFilterTest {

    @MockK
    lateinit var jwtValidator : JwtValidator

    @MockK
    lateinit var tokenRevocationChecker : TokenRevocationChecker

    private fun filter(failOpen: Boolean = true) =
        JwtAuthenticationFilter(
            jwtValidator,
            tokenRevocationChecker,
            listOf("POST", "PUT", "PATCH", "DELETE"),
            failOpen
        )

    private fun aValidJwt() = ValidatedJwt(
        jti = UUID.randomUUID(), subject = UUID.randomUUID(), tenantId = UUID.randomUUID(),
        principalType = "user", roles = emptyList(), modules = emptyList(),
        mustChangePassword = false,
        expiresAt = Instant.now().plusSeconds(900), issuedAt = Instant.now()
    )


    @Test
    fun `should return 401 when jti is revoked on sensitive operation`() {
        every { jwtValidator.validate(any()) } returns aValidJwt()
        every { tokenRevocationChecker.checkRevocation(any(), any(), any()) } returns RevocationReason.JTI

        val req = MockHttpServletRequest("POST", "/x").apply { addHeader("Authorization", "Bearer x") }
        val resp = MockHttpServletResponse()

        filter().doFilter(req, resp, MockFilterChain())

        assertEquals(401, resp.status)
    }

    @Test
    fun `should pass on GET without checking revocation`(){
        every { jwtValidator.validate(any()) } returns aValidJwt()

        val req = MockHttpServletRequest("GET", "/x").apply { addHeader("Authorization", "Bearer x") }
        val resp = MockHttpServletResponse()

        filter().doFilter(req, resp, MockFilterChain() )

        assertEquals(200, resp.status)

        verify(exactly = 0) { tokenRevocationChecker.checkRevocation(any(), any(), any()) }
    }

    @Test
    fun `should pass when redis fails and fail-open is true`() {
        every { jwtValidator.validate(any()) } returns aValidJwt()
        every { tokenRevocationChecker.checkRevocation(any(), any(), any()) } throws RuntimeException("redis down")

        val req = MockHttpServletRequest("POST", "/x").apply { addHeader("Authorization", "Bearer x") }
        val resp = MockHttpServletResponse()

        filter(failOpen = true).doFilter(req, resp, MockFilterChain())

        assertEquals(200, resp.status)
    }

    @Test
    fun `should return 401 when redis is down and fail-closed`(){
        every { jwtValidator.validate(any()) } returns aValidJwt()
        every{ tokenRevocationChecker.checkRevocation(any(), any(), any()) } throws RuntimeException("redis down")

        val req = MockHttpServletRequest("POST", "/x").apply { addHeader("Authorization", "Bearer x") }
        val resp = MockHttpServletResponse()

        filter(failOpen = false).doFilter(req, resp, MockFilterChain())

        assertEquals(401, resp.status)
    }

}