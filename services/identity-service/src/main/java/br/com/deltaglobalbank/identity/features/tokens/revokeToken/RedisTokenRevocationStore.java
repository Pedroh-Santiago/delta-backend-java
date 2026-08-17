package br.com.deltaglobalbank.identity.features.tokens.revokeToken;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.TokenRevocationStore;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties;
import br.com.deltaglobalbank.sharedauth.RevocationReason;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenRevocationStore implements TokenRevocationStore {

    private final StringRedisTemplate redis;
    private final JwtIssuerProperties jwtProperties;

    public RedisTokenRevocationStore(StringRedisTemplate redis, JwtIssuerProperties jwtProperties) {
        this.redis = redis;
        this.jwtProperties = jwtProperties;
    }

    private String jtiKey(String jti) {
        return "revoked:jti:" + jti;
    }

    private String userSinceKey(UUID userId) {
        return "revoked:user:" + userId + ":since";
    }

    @Override
    public void revokeJti(String jti, Duration ttl) {
        redis.opsForValue().set(jtiKey(jti), "", ttl);
    }

    @Override
    public void revokeUser(UUID userId) {
        redis.opsForValue().set(userSinceKey(userId), Instant.now().toString(), jwtProperties.accessTokenTtl());
    }

    @Override
    public RevocationReason checkRevocation(String jti, UUID userId, Instant issuedAt) {
        if (Boolean.TRUE.equals(redis.hasKey(jtiKey(jti)))) {
            return RevocationReason.JTI;
        }
        String since = redis.opsForValue().get(userSinceKey(userId));
        if (since != null && issuedAt.isBefore(Instant.parse(since))) {
            return RevocationReason.USER;
        }
        return null;
    }
}
