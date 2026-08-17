package br.com.deltaglobalbank.identity.infrastructure.security.apikey

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.api-key")
data class ApiKeyProperties(
    val environment: String,
    val fingerprintSecret: String,
    val brand: String
) {
    init {
        require(environment in setOf("live", "test")) {
            "identity.api-key.environment deve ser 'live' ou 'test', recebeu '$environment'"
        }
        require(fingerprintSecret.length >= 16) {
            "identity.api-key.fingerprint-secret precisa ter pelo menos 16 caracteres"
        }
        require(brand.matches(Regex("^[a-z]{2,5}$"))) {
            "identity.api-key.brand deve ter 2-5 chars minúsculos"
        }
    }
}