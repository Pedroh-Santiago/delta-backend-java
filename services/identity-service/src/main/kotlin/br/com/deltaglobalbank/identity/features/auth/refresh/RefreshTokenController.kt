package br.com.deltaglobalbank.identity.features.auth.refresh

import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException
import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class RefreshTokenController(
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val refreshTokenCookie: RefreshTokenCookie
) {
    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody(required = false) request: RefreshTokenRequest?,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<RefreshTokenResponse> {
        val token = refreshTokenCookie.read(httpRequest)
            ?: request?.refreshToken
            ?: throw InvalidRefreshTokenException()

        val context = RefreshTokenContext(
            ipAddress = resolveClientIp(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent"),
        )

        val result = refreshTokenUseCase.execute(token, context)
        refreshTokenCookie.set(httpResponse, result.refreshToken)

        return ResponseEntity.ok(
            RefreshTokenResponse(
                accessToken = result.accessToken,
                tokenType = result.tokenType,
                expiresIn = result.expiresIn,
                mustChangePassword = result.mustChangePassword,
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