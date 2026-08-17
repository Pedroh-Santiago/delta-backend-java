package br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ExchangeApiKeyCache(
    private val redisTemplate: StringRedisTemplate,
    private val properties: ExchangeApiKeyProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun get(fingerprint: String): String? {
        return try {
            redisTemplate.opsForValue().get(key(fingerprint))
        } catch (ex: Exception) {
            log.warn("Falha ao ler cache ExchangeApiKey (fail-open): {}", ex.message)
            null
        }
    }

    fun set(fingerprint: String, token: String, ttlSeconds: Long) {
        val effectiveTtl = (ttlSeconds - properties.cacheTtlMarginSeconds).coerceAtLeast(1)
        try {
            redisTemplate.opsForValue().set(
                key(fingerprint),
                token,
                Duration.ofSeconds(effectiveTtl)
            )
        } catch (ex: Exception) {
            log.warn("Falha ao escrever cache ExchangeApiKey: {}", ex.message)
        }
    }

    fun invalidate(fingerprint: String) {
        try {
            redisTemplate.delete(key(fingerprint))
        } catch (ex: Exception) {
            log.warn("Falha ao invalidar cache ExchangeApiKey: {}", ex.message)
        }
    }

    private fun key(fingerprint: String): String = "apikey:token:$fingerprint"
}