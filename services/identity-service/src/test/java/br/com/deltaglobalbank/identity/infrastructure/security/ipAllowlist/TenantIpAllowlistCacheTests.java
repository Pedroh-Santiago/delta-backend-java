package br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

class TenantIpAllowlistCacheTests {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final TenantIpAllowlistProperties properties = new TenantIpAllowlistProperties(300);

    private final ObjectMapper mapper = new ObjectMapper();

    private TenantIpAllowlistCache cache;

    @BeforeEach
    void setUp() {
        cache = new TenantIpAllowlistCache(redisTemplate, mapper, properties);
    }

    @Test
    void getMustReturnNullWhenRedisFails() {
        UUID tenantId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        List<String> result = cache.get(tenantId);

        assertNull(result);
    }

    @Test
    void getMustDeserializeCachedCidrs() {
        UUID tenantId = UUID.randomUUID();
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn("[\"192.168.0.0/24\",\"10.0.0.0/8\"]");

        List<String> result = cache.get(tenantId);

        assertEquals(List.of("192.168.0.0/24", "10.0.0.0/8"), result);
    }

    @Test
    void setMustNotThrowWhenRedisFails() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        assertDoesNotThrow(() -> cache.set(UUID.randomUUID(), List.of("192.168.0.0/24")));
    }

    @Test
    void invalidateMustNotThrowWhenRedisFails() {
        when(redisTemplate.delete(org.mockito.ArgumentMatchers.anyString())).thenThrow(new RuntimeException("redis down"));

        assertDoesNotThrow(() -> cache.invalidate(UUID.randomUUID()));
    }
}
