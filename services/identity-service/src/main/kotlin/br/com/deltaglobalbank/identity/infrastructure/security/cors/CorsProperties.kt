package br.com.deltaglobalbank.identity.infrastructure.security.cors

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = emptyList(),
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "PATCH", "DELETE"),
    val allowedHeaders: List<String> = listOf("*"),
    val allowCredentials: Boolean = true,
    val maxAge: Long = 3600
)