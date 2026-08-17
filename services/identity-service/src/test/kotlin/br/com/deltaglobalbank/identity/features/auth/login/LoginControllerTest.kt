package br.com.deltaglobalbank.identity.features.auth.login

import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class LoginControllerTest {

    private val loginUseCase = mockk<LoginUseCase>()
    private val refreshCookie = mockk<RefreshTokenCookie>(relaxed = true)
    private val controller = LoginController(loginUseCase, refreshCookie)

    private val loginRequest = mockk<LoginRequest>(relaxed = true)
    private val httpRequest = mockk<HttpServletRequest>(relaxed = true)
    private val httpResponse = mockk<HttpServletResponse>(relaxed = true)

    @Test
    fun `should set refresh token cookie and return body without refresh on login`(){
        every { loginUseCase.execute(any(), any()) } returns LoginResult(accessToken = "access", refreshToken = "refresh", expiresIn = 900, mustChangePassword = false)

        val response = controller.login(loginRequest, httpRequest, httpResponse)

        verify { refreshCookie.set(httpResponse, "refresh") }
        assertEquals(HttpStatus.OK, response.statusCode)

        val body = response.body!!
        assertEquals("access", body.accessToken)
        assertEquals(900, body.expiresIn)
        assertEquals(false, body.mustChangePassword)
    }

    @Test
    fun `should resolve client ip from x forwarded for`(){
        every { httpRequest.getHeader("X-Forwarded-For") } returns "192.168.1.1"
        every { httpRequest.getHeader("User-Agent") } returns "user-agent"

        val ctx = slot<LoginContext>()
        every { loginUseCase.execute(any(), capture(ctx)) } returns LoginResult(accessToken = "access", refreshToken = "refresh", expiresIn = 900, mustChangePassword = false)

        controller.login(loginRequest, httpRequest, httpResponse)

        assertEquals("192.168.1.1", ctx.captured.ipAddress)
        assertEquals("user-agent", ctx.captured.userAgent)
    }

    @Test
    fun `should fall back to remote addr when x forwarded for is absent`(){
        every { httpRequest.getHeader("X-Forwarded-For") } returns null
        every { httpRequest.remoteAddr } returns "192.168.1.5"
        val ctx = slot<LoginContext>()
        every { loginUseCase.execute(any(), capture(ctx)) } returns LoginResult("A","R",expiresIn=900,mustChangePassword=false)

        controller.login(loginRequest, httpRequest, httpResponse)

        assertEquals("192.168.1.5", ctx.captured.ipAddress)
    }
}