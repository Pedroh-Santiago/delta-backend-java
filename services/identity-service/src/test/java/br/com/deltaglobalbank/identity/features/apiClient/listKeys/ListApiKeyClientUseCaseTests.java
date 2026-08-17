package br.com.deltaglobalbank.identity.features.apiClient.listKeys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException;
import br.com.deltaglobalbank.identity.features.apiClients.listApiKeys.ClientApiKeyResponse;
import br.com.deltaglobalbank.identity.features.apiClients.listApiKeys.ListApiKeyClientUseCase;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ListApiKeyClientUseCaseTests {

    private final ApiClientRepository apiClientRepository = mock(ApiClientRepository.class);
    private final JpaApiKeyRepository jpaApiKeyRepository = mock(JpaApiKeyRepository.class);

    private ListApiKeyClientUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListApiKeyClientUseCase(apiClientRepository, jpaApiKeyRepository);
    }

    @Test
    void shouldThrowApiClientNotFoundExceptionWhenApiClientBelongsToAnotherTenant() {
        UUID apiClientId = UUID.randomUUID();
        UUID actingTenantId = UUID.randomUUID();
        UUID otherTenantId = UUID.randomUUID();

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.getTenantId()).thenReturn(otherTenantId);
        when(apiClientRepository.findById(apiClientId)).thenReturn(apiClient);

        assertThrows(ApiClientNotFoundException.class, () -> useCase.getClientApiKey(apiClientId, actingTenantId, 0, 20));

        verify(jpaApiKeyRepository, times(0)).findByApiClientId(any(), any());
    }

    @Test
    void shouldThrowApiClientNotFoundExceptionWhenApiClientDoesNotExist() {
        UUID apiClientId = UUID.randomUUID();
        when(apiClientRepository.findById(apiClientId)).thenReturn(null);

        assertThrows(ApiClientNotFoundException.class, () -> useCase.getClientApiKey(apiClientId, UUID.randomUUID(), 0, 20));
        verify(jpaApiKeyRepository, times(0)).findByApiClientId(any(), any());
    }

    @Test
    void shouldListAllKeysIncludingRevokedAndExpiredWithTheirState() {
        UUID apiClientId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Instant now = Instant.now();

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.getTenantId()).thenReturn(tenantId);
        when(apiClientRepository.findById(apiClientId)).thenReturn(apiClient);

        ApiKeyEntity activeKey = mock(ApiKeyEntity.class);
        when(activeKey.getId()).thenReturn(UUID.randomUUID());
        when(activeKey.getName()).thenReturn("active-key");
        when(activeKey.getKeyPrefix()).thenReturn("dgb_live_a");
        when(activeKey.getExpiresAt()).thenReturn(null);
        when(activeKey.getRevokedAt()).thenReturn(null);
        when(activeKey.getLastUsedAt()).thenReturn(null);
        when(activeKey.getCreatedAt()).thenReturn(now);

        ApiKeyEntity revokedKey = mock(ApiKeyEntity.class);
        when(revokedKey.getId()).thenReturn(UUID.randomUUID());
        when(revokedKey.getName()).thenReturn("revoked-key");
        when(revokedKey.getKeyPrefix()).thenReturn("dgb_live_b");
        when(revokedKey.getExpiresAt()).thenReturn(null);
        when(revokedKey.getRevokedAt()).thenReturn(now.minusSeconds(3600));
        when(revokedKey.getLastUsedAt()).thenReturn(null);
        when(revokedKey.getCreatedAt()).thenReturn(now.minusSeconds(7200));

        ApiKeyEntity expiredKey = mock(ApiKeyEntity.class);
        when(expiredKey.getId()).thenReturn(UUID.randomUUID());
        when(expiredKey.getName()).thenReturn("expired-key");
        when(expiredKey.getKeyPrefix()).thenReturn("dgb_live_c");
        when(expiredKey.getExpiresAt()).thenReturn(now.minusSeconds(3600));
        when(expiredKey.getRevokedAt()).thenReturn(null);
        when(expiredKey.getLastUsedAt()).thenReturn(null);
        when(expiredKey.getCreatedAt()).thenReturn(now.minusSeconds(7200));

        List<ApiKeyEntity> keys = List.of(activeKey, revokedKey, expiredKey);
        when(jpaApiKeyRepository.findByApiClientId(any(), any()))
            .thenReturn(new PageImpl<>(keys, PageRequest.of(0, 20), keys.size()));

        ClientApiKeyResponse response = useCase.getClientApiKey(apiClientId, tenantId, 0, 20);

        assertEquals(3, response.items().size());
        assertEquals(1, response.items().stream().filter(it -> it.revokedAt() != null).count());
        assertEquals(1, response.items().stream()
            .filter(it -> it.expiresAt() != null && it.expiresAt().isBefore(now)).count());
    }
}
