package br.com.deltaglobalbank.identity.infrastructure.security.cookie

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.refresh-cookie")
data class RefreshTokenCookieProperties(
    val name: String = "refreshToken",
    val domain: String? = null,
    val secure: Boolean = true,
    val sameSite: String = "Strict",
    val path: String = "/auth"
)