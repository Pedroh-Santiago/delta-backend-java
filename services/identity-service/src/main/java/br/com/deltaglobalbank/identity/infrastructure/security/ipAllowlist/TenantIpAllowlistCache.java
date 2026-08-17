package br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TenantIpAllowlistCache {

    private static final Logger log = LoggerFactory.getLogger(TenantIpAllowlistCache.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;
    private final TenantIpAllowlistProperties properties;

    public TenantIpAllowlistCache(
        StringRedisTemplate redisTemplate,
        ObjectMapper mapper,
        TenantIpAllowlistProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
        this.properties = properties;
    }

    private String key(UUID tenantId) {
        return "tenant:ip-allowlist:" + tenantId;
    }

    public List<String> get(UUID tenantId) {
        try {
            String value = redisTemplate.opsForValue().get(key(tenantId));
            if (value == null) {
                return null;
            }
            return Arrays.asList(mapper.readValue(value, String[].class));
        } catch (Exception e) {
            log.warn("Falha ao ler cache IP allowlist (fail-open): {}", e.getMessage());
            return null;
        }
    }

    public void set(UUID tenantId, List<String> cidrs) {
        try {
            redisTemplate.opsForValue().set(
                key(tenantId),
                mapper.writeValueAsString(cidrs),
                Duration.ofSeconds(properties.cacheTtlSeconds())
            );
        } catch (Exception e) {
            log.warn("Falha ao gravar cache IP allowlist: {}", e.getMessage());
        }
    }

    public void invalidate(UUID tenantId) {
        try {
            redisTemplate.delete(key(tenantId));
        } catch (Exception e) {
            log.warn("Falha ao invalidar cache IP allowlist: {}", e.getMessage());
        }
    }
}
