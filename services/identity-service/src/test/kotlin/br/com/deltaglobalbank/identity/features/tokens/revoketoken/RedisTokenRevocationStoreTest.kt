package br.com.deltaglobalbank.identity.features.tokens.revoketoken

import br.com.deltaglobalbank.identity.features.tokens.revokeToken.RedisTokenRevocationStore
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import br.com.deltaglobalbank.sharedauth.RevocationReason
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Testcontainers
class RedisTokenRevocationStoreTest {

    companion object {
        @Container
        @JvmStatic
        val redis = GenericContainer<Nothing>("redis:7-alpine").apply { withExposedPorts(6379) }
    }

    private lateinit var store: RedisTokenRevocationStore

    @BeforeEach
    fun setUp() {
        val factory = LettuceConnectionFactory(redis.host, redis.firstMappedPort)
        factory.afterPropertiesSet()
        val template = StringRedisTemplate(factory)
        template.afterPropertiesSet()
        store = RedisTokenRevocationStore(template, JwtIssuerProperties())
    }

    @Test
    fun `should mark jti as revoked`() {
        val jti = UUID.randomUUID().toString()
        store.revokeJti(jti, Duration.ofMinutes(5))

        val reason = store.checkRevocation(jti, UUID.randomUUID(), Instant.now())

        assertEquals(RevocationReason.JTI, reason)
    }

    @Test
    fun `should revoke user tokens issued before since`(){
        val userId = UUID.randomUUID()

        store.revokeUser(userId)

        val issuedAt = Instant.now().minusSeconds(10)

        val old = store.checkRevocation(UUID.randomUUID().toString(), userId, issuedAt)
        assertEquals(RevocationReason.USER, old)

        val new  = store.checkRevocation(UUID.randomUUID().toString(), userId, Instant.now().plusSeconds(60))
        assertNull(new)
    }

    @Test
    fun `should return null when nothing is revoked`(){
        assertEquals(null, store.checkRevocation(UUID.randomUUID().toString(), UUID.randomUUID(), Instant.now()))
    }

    @Test
    fun `should expire jti key after ttl`(){
        val jti = UUID.randomUUID().toString()

        store.revokeJti(jti, Duration.ofSeconds(1))

        Thread.sleep(1500)

        assertNull(store.checkRevocation(jti, UUID.randomUUID(), Instant.now()))

    }
}