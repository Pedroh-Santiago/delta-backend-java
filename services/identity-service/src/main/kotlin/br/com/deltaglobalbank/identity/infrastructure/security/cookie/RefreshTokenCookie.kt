package br.com.deltaglobalbank.identity.infrastructure.security.cookie

import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import org.springframework.http.HttpHeaders
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenCookie (
    private val props: RefreshTokenCookieProperties,
    private val jwtProps: JwtIssuerProperties
){
    fun set(response: HttpServletResponse, token: String) =
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, jwtProps.refreshTokenTtl).toString())

    fun clear(response: HttpServletResponse) =
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())

    fun read(request: HttpServletRequest): String? =
        request.cookies?.firstOrNull { it.name == props.name }?.value

    private fun cookie(value: String, maxAge: Duration) : ResponseCookie {
        val b = ResponseCookie.from(props.name, value)
            .httpOnly(true).secure(props.secure).sameSite(props.sameSite)
            .path(props.path).maxAge(maxAge)
        props.domain?.let { b.domain(it) }
        return b.build()
    }
}