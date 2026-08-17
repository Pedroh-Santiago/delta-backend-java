package br.com.deltaglobalbank.identity.features.tokens.revokeToken

import br.com.deltaglobalbank.identity.domain.token.TokenRevocationStore
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import br.com.deltaglobalbank.sharedauth.RevocationReason
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import java.time.Duration

@Component
class RedisTokenRevocationStore(private val redis: StringRedisTemplate, private val jwtProperties: JwtIssuerProperties) :
    TokenRevocationStore {


    private fun jtiKey(jti: String) = "revoked:jti:$jti"
    private fun userSinceKey(userId: UUID) = "revoked:user:$userId:since"

    override fun revokeJti(jti: String, ttl:Duration) {
        redis.opsForValue().set(jtiKey(jti), "", ttl)
    }

    override fun revokeUser(userId: UUID) {
        redis.opsForValue().set(userSinceKey(userId), Instant.now().toString(), jwtProperties.accessTokenTtl)
    }

    override fun checkRevocation(jti: String, userId: UUID, issuedAt: Instant): RevocationReason? {
        if (redis.hasKey(jtiKey(jti))) return RevocationReason.JTI
        val since = redis.opsForValue().get(userSinceKey(userId))
        if (since != null && issuedAt.isBefore(Instant.parse(since))) return RevocationReason.USER
        return null
    }

}
