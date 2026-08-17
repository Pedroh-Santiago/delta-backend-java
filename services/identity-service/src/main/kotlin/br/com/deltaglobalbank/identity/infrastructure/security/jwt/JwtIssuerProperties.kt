package br.com.deltaglobalbank.identity.infrastructure.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "identity.jwt")
data class JwtIssuerProperties(
    val issuer: String = "identity-service",
    val audience: String = "internal",
    val accessTokenTtl: Duration = Duration.ofMinutes(15),
    val refreshTokenTtl: Duration = Duration.ofDays(7)
)
