package br.com.deltaglobalbank.sharedauth.revocation;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.RevocationReason;
import br.com.deltaglobalbank.sharedauth.TokenRevocationChecker;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisTokenRevocationChecker implements TokenRevocationChecker {

    private final StringRedisTemplate redis;

    public RedisTokenRevocationChecker(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public RevocationReason checkRevocation(UUID jti, UUID userId, Instant issuedAt) {
        if (Boolean.TRUE.equals(redis.hasKey(RevocationKeys.jti(jti.toString())))) {
            return RevocationReason.JTI;
        }

        String since = redis.opsForValue().get(RevocationKeys.userSince(userId));
        if (since != null && issuedAt.isBefore(Instant.parse(since))) {
            return RevocationReason.USER;
        }

        return null;
    }
}
