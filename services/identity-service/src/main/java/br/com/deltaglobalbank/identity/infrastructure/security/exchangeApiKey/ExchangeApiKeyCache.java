package br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExchangeApiKeyCache {

    private static final Logger log = LoggerFactory.getLogger(ExchangeApiKeyCache.class);

    private final StringRedisTemplate redisTemplate;
    private final ExchangeApiKeyProperties properties;

    public ExchangeApiKeyCache(StringRedisTemplate redisTemplate, ExchangeApiKeyProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public String get(String fingerprint) {
        try {
            return redisTemplate.opsForValue().get(key(fingerprint));
        } catch (Exception ex) {
            log.warn("Falha ao ler cache ExchangeApiKey (fail-open): {}", ex.getMessage());
            return null;
        }
    }

    public void set(String fingerprint, String token, long ttlSeconds) {
        long effectiveTtl = Math.max(ttlSeconds - properties.cacheTtlMarginSeconds(), 1);
        try {
            redisTemplate.opsForValue().set(key(fingerprint), token, Duration.ofSeconds(effectiveTtl));
        } catch (Exception ex) {
            log.warn("Falha ao escrever cache ExchangeApiKey: {}", ex.getMessage());
        }
    }

    public void invalidate(String fingerprint) {
        try {
            redisTemplate.delete(key(fingerprint));
        } catch (Exception ex) {
            log.warn("Falha ao invalidar cache ExchangeApiKey: {}", ex.getMessage());
        }
    }

    private String key(String fingerprint) {
        return "apikey:token:" + fingerprint;
    }
}
