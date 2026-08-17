package br.com.deltaglobalbank.sharedauth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.security")
data class RevocationProperties(
    val revocationCheckOn: List<String> = listOf("POST", "PUT", "PATCH", "DELETE"),
    val failOpenOnRedisError: Boolean = true
)