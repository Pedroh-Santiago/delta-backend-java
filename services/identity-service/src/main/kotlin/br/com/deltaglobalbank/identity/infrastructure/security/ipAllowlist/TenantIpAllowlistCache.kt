package br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID
import com.fasterxml.jackson.databind.ObjectMapper

@Component
class TenantIpAllowlistCache(
    private val redisTemplate: StringRedisTemplate,
    private val mapper: ObjectMapper,
    private val properties: TenantIpAllowlistProperties
)  {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun key(tenantId: UUID) = "tenant:ip-allowlist:$tenantId"

    fun get(tenantId: UUID): List<String>? = try {
        redisTemplate.opsForValue().get(key(tenantId))
            ?.let { mapper.readValue(it, Array<String>::class.java).toList() }
    } catch (e: Exception) {
        log.warn("Falha ao ler cache IP allowlist (fail-open): {}", e.message)
        null
    }

    fun set(tenantId: UUID, cidrs: List<String>) = try {
        redisTemplate.opsForValue().set(
            key(tenantId),
            mapper.writeValueAsString(cidrs),
            Duration.ofSeconds(properties.cacheTtlSeconds)       // ou properties.cacheTtlSeconds (Etapa 2)
        )
    } catch (e: Exception) {
        log.warn("Falha ao gravar cache IP allowlist: {}", e.message)
    }

    fun invalidate(tenantId: UUID) = try {
        redisTemplate.delete(key(tenantId))
    } catch (e: Exception) {
        log.warn("Falha ao invalidar cache IP allowlist: {}", e.message)
    }
}