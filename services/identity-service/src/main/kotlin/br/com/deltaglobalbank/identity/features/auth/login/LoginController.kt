package br.com.deltaglobalbank.identity.features.auth.login

import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class LoginController(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenCookie: RefreshTokenCookie
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val context = LoginContext(
            ipAddress = resolveClientIp(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )
        val result = loginUseCase.execute(request, context)
        refreshTokenCookie.set(httpResponse, result.refreshToken)

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = result.accessToken,
                tokenType = result.tokenType,
                expiresIn = result.expiresIn,
                mustChangePassword = result.mustChangePassword
            )
        )
    }

    private fun resolveClientIp(request: HttpServletRequest): String? {
        return request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: request.remoteAddr
    }
}