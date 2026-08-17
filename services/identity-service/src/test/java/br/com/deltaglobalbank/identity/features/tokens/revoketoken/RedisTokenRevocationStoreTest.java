package br.com.deltaglobalbank.identity.features.tokens.revoketoken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.features.tokens.revokeToken.RedisTokenRevocationStore;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties;
import br.com.deltaglobalbank.sharedauth.RevocationReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class RedisTokenRevocationStoreTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private RedisTokenRevocationStore store;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redis.getHost(), redis.getFirstMappedPort());
        factory.afterPropertiesSet();
        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        store = new RedisTokenRevocationStore(template, new JwtIssuerProperties(
            "identity-service", "internal", Duration.ofMinutes(15), Duration.ofDays(7)));
    }

    @Test
    void shouldMarkJtiAsRevoked() {
        String jti = UUID.randomUUID().toString();
        store.revokeJti(jti, Duration.ofMinutes(5));

        RevocationReason reason = store.checkRevocation(jti, UUID.randomUUID(), Instant.now());

        assertEquals(RevocationReason.JTI, reason);
    }

    @Test
    void shouldRevokeUserTokensIssuedBeforeSince() {
        UUID userId = UUID.randomUUID();

        store.revokeUser(userId);

        Instant issuedAt = Instant.now().minusSeconds(10);

        RevocationReason old = store.checkRevocation(UUID.randomUUID().toString(), userId, issuedAt);
        assertEquals(RevocationReason.USER, old);

        RevocationReason newer = store.checkRevocation(UUID.randomUUID().toString(), userId, Instant.now().plusSeconds(60));
        assertNull(newer);
    }

    @Test
    void shouldReturnNullWhenNothingIsRevoked() {
        assertEquals(null, store.checkRevocation(UUID.randomUUID().toString(), UUID.randomUUID(), Instant.now()));
    }

    @Test
    void shouldExpireJtiKeyAfterTtl() throws InterruptedException {
        String jti = UUID.randomUUID().toString();

        store.revokeJti(jti, Duration.ofSeconds(1));

        Thread.sleep(1500);

        assertNull(store.checkRevocation(jti, UUID.randomUUID(), Instant.now()));
    }
}
