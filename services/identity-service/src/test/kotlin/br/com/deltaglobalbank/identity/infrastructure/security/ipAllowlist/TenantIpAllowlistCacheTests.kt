package br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class TenantIpAllowlistCacheTests {

    @MockK
    lateinit var redisTemplate: StringRedisTemplate

    @MockK
    lateinit var properties: TenantIpAllowlistProperties

    private val mapper = ObjectMapper()

    private lateinit var cache: TenantIpAllowlistCache

    @BeforeEach
    fun setUp() {
        cache = TenantIpAllowlistCache(redisTemplate, mapper, properties)
    }

    @Test
    fun `get must return null when redis fails`() {
        val tenantId = UUID.randomUUID()
        every { redisTemplate.opsForValue() } throws RuntimeException("redis down")

        val result = cache.get(tenantId)

        assertNull(result)
    }

    @Test
    fun `get must deserialize cached cidrs`() {
        val tenantId = UUID.randomUUID()
        every { redisTemplate.opsForValue().get(any()) } returns """["192.168.0.0/24","10.0.0.0/8"]"""

        val result = cache.get(tenantId)

        assertEquals(listOf("192.168.0.0/24", "10.0.0.0/8"), result)
    }

    @Test
    fun `set must not throw when redis fails`() {
        every { redisTemplate.opsForValue() } throws RuntimeException("redis down")

        assertDoesNotThrow {
            cache.set(UUID.randomUUID(), listOf("192.168.0.0/24"))
        }
    }

    @Test
    fun `invalidate must not throw when redis fails`() {
        every { redisTemplate.delete(any<String>()) } throws RuntimeException("redis down")

        assertDoesNotThrow {
            cache.invalidate(UUID.randomUUID())
        }
    }
}