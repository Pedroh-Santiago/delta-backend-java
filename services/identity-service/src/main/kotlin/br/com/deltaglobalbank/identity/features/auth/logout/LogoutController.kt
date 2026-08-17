package br.com.deltaglobalbank.identity.features.auth.logout

import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController()
@RequestMapping("/auth")
class LogoutController(
    private val logoutUseCase:LogoutUseCase,
    private val refreshTokenCookie: RefreshTokenCookie
) {

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @RequestParam(defaultValue = "false") allSessions: Boolean,
        @RequestBody(required = false) request: LogoutRequest?,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<Void> {
        val token = refreshTokenCookie.read(httpRequest) ?: request?.refreshToken

        logoutUseCase.logout(principal.subject, principal.jti, token, allSessions)
        refreshTokenCookie.clear(httpResponse)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
