package br.com.deltaglobalbank.identity.infrastructure.security.cookie

import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class RefreshTokenCookieTest {

    private val cookie = RefreshTokenCookie(RefreshTokenCookieProperties(), JwtIssuerProperties())
    private val resp = mockk<HttpServletResponse>(relaxed = true)
    private val header = slot<String>()

    @Test
    fun `should set httpOnly secure cookie with configured attributes`() {
        every { resp.addHeader("Set-Cookie", capture(header)) } just Runs

        cookie.set(resp, "raw")

        assertTrue(header.captured.contains("refreshToken=raw"))
        assertTrue(header.captured.contains("HttpOnly"))
        assertTrue(header.captured.contains("Secure"))
        assertTrue(header.captured.contains("SameSite=Strict"))
        assertTrue(header.captured.contains("Path=/auth"))
        assertFalse(header.captured.contains("Max-Age=0"))
    }

    @Test
    fun `should clear cookie with max age zero`() {
        every { resp.addHeader("Set-Cookie", capture(header)) } just Runs

        cookie.clear(resp)

        assertTrue { header.captured.contains("Max-Age=0") }
        assertTrue { header.captured.contains("Path=/auth") }
    }

    @Test
    fun `should read token when cookie is present and return null when absent`() {
        val req = mockk<HttpServletRequest>()

        every {req.cookies} returns arrayOf(Cookie("refreshToken", "raw"))
        assertEquals("raw", cookie.read(req))

        every { req.cookies } returns null
        assertNull(cookie.read(req))

        every { req.cookies } returns arrayOf(Cookie("any", "x"))
        assertNull(cookie.read(req))
    }

    @Test
    fun `should honor custom secure sameSite and domain properties`(){
        val custom = RefreshTokenCookie(
            RefreshTokenCookieProperties(secure = false, sameSite = "Lax", domain = "delta.com"), JwtIssuerProperties()
        )

        every { resp.addHeader("Set-Cookie", capture(header)) } just Runs
        custom.set(resp, "raw")

        assertFalse(header.captured.contains("Secure"));
        assertTrue(header.captured.contains("SameSite=Lax"));
        assertTrue(header.captured.contains("Domain=delta.com"))
    }
}