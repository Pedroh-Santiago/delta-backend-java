package br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.exchange-api-key")
data class ExchangeApiKeyProperties(
    val cacheTtlMarginSeconds: Long = 30
) {
    init {
        require(cacheTtlMarginSeconds in 0..120) {
            "identity.exchange-api-key.cache-ttl-margin-seconds deve estar entre 0 e 120"
        }
    }
}