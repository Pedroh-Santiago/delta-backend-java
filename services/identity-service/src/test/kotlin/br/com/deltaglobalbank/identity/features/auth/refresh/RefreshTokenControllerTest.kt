package br.com.deltaglobalbank.identity.features.auth.refresh

import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException
import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class RefreshTokenControllerTest {

    private val useCase = mockk<RefreshTokenUseCase>()
    private val refreshCookie = mockk<RefreshTokenCookie>(relaxed = true)
    private val controller = RefreshTokenController(useCase, refreshCookie)

    private val httpRequest = mockk<HttpServletRequest>(relaxed = true)
    private val httpResponse = mockk<HttpServletResponse>(relaxed = true)

    @Test
    fun `should read token from cookie and rotate cookie on refresh`(){
        every { refreshCookie.read(httpRequest) } returns "cookie-token"
        every { useCase.execute("cookie-token", any()) } returns RefreshTokenResult(accessToken = "access", refreshToken = "refresh", expiresIn=900, mustChangePassword=false)

        val response = controller.refreshToken(null, httpRequest, httpResponse)

        verify { useCase.execute("cookie-token", any()) }
        verify { refreshCookie.set(httpResponse, "refresh") }
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("access", response.body!!.accessToken)
    }

    @Test
    fun `should fall back to body token when cookie is absent`(){
        every { refreshCookie.read(httpRequest) } returns null
        every { useCase.execute("body-token", any()) } returns
                RefreshTokenResult("access", "refresh", expiresIn = 900, mustChangePassword = false)

        controller.refreshToken(RefreshTokenRequest("body-token"), httpRequest, httpResponse)

        verify { useCase.execute("body-token", any()) }
    }

    @Test
    fun `should throw when neither cookie nor body has a token`(){
        every { refreshCookie.read(httpRequest) } returns null

        assertThrows(InvalidRefreshTokenException::class.java) {
            controller.refreshToken(null, httpRequest, httpResponse)
        }
        verify(exactly = 0) { useCase.execute(any(), any()) }
    }

}