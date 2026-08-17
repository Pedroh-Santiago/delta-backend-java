package br.com.deltaglobalbank.sharedauth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.jwt")
data class JwtValidationProperties(
    val jwksUrl: String = "",
    val expectedIssuer: String = "identity-service",
    val expectedAudience: String = "internal"
)
