package br.com.deltaglobalbank.identity.features.apiClient.revokeApiKey;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyNotFoundException;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository;
import br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey.RevokeApiKeyCommand;
import br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey.RevokeApiKeyUseCase;
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeApiKeyUseCaseTests {

    private final ApiKeyRepository apiKeyRepository = mock(ApiKeyRepository.class);
    private final ApiClientRepository apiClientRepository = mock(ApiClientRepository.class);
    private final ExchangeApiKeyCache cache = mock(ExchangeApiKeyCache.class);

    private RevokeApiKeyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RevokeApiKeyUseCase(apiKeyRepository, apiClientRepository, cache);
    }

    @Test
    void mustRevokeApiKeyAndInvalidateCacheWhenTenantMatches() {
        UUID apiKeyId = UUID.randomUUID();
        UUID apiClientId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String fingerprint = "fingerprint";
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(apiKeyId, tenantId, UUID.randomUUID());

        ApiKey apiKey = mock(ApiKey.class);
        when(apiKey.getApiClientId()).thenReturn(apiClientId);
        when(apiKey.getFingerprint()).thenReturn(fingerprint);
        when(apiKey.getId()).thenReturn(apiKeyId);
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(apiKey);

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.getTenantId()).thenReturn(tenantId);
        when(apiClientRepository.findById(apiClientId)).thenReturn(apiClient);

        when(apiKeyRepository.save(apiKey)).thenReturn(apiKey);
        doNothing().when(cache).invalidate(fingerprint);

        useCase.execute(command);

        verify(apiKey, times(1)).revoke();
        verify(apiKeyRepository, times(1)).save(apiKey);
        verify(cache, times(1)).invalidate(fingerprint);
    }

    @Test
    void mustRevokeWithoutTenantCheckWhenTenantIdIsNull() {
        UUID apiKeyId = UUID.randomUUID();
        String fingerprint = "fingerprint";
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(apiKeyId, null, UUID.randomUUID());

        ApiKey apiKey = mock(ApiKey.class);
        when(apiKey.getFingerprint()).thenReturn(fingerprint);
        when(apiKey.getId()).thenReturn(apiKeyId);
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(apiKey);
        when(apiKeyRepository.save(apiKey)).thenReturn(apiKey);
        doNothing().when(cache).invalidate(fingerprint);

        useCase.execute(command);

        verify(apiClientRepository, times(0)).findById(any());
        verify(apiKey, times(1)).revoke();
        verify(cache, times(1)).invalidate(fingerprint);
    }

    @Test
    void mustThrowWhenApiKeyBelongsToADifferentTenant() {
        UUID apiKeyId = UUID.randomUUID();
        UUID apiClientId = UUID.randomUUID();
        UUID ownerTenantId = UUID.randomUUID();
        UUID attackerTenantId = UUID.randomUUID();
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(apiKeyId, attackerTenantId, UUID.randomUUID());

        ApiKey apiKey = mock(ApiKey.class);
        when(apiKey.getApiClientId()).thenReturn(apiClientId);
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(apiKey);

        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.getTenantId()).thenReturn(ownerTenantId);
        when(apiClientRepository.findById(apiClientId)).thenReturn(apiClient);

        assertThrows(ApiKeyNotFoundException.class, () -> useCase.execute(command));

        verify(apiKeyRepository, times(0)).save(any());
        verify(cache, times(0)).invalidate(any());
    }

    @Test
    void mustThrowWhenApiKeyDoesNotExist() {
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(apiKeyRepository.findById(command.apiKeyId())).thenReturn(null);

        assertThrows(ApiKeyNotFoundException.class, () -> useCase.execute(command));
        verify(apiKeyRepository, times(0)).save(any());
        verify(cache, times(0)).invalidate(any());
    }

    @Test
    void mustThrowWhenApiClientDoesNotExist() {
        UUID apiKeyId = UUID.randomUUID();
        UUID apiClientId = UUID.randomUUID();
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(apiKeyId, UUID.randomUUID(), UUID.randomUUID());

        ApiKey apiKey = mock(ApiKey.class);
        when(apiKey.getApiClientId()).thenReturn(apiClientId);
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(apiKey);

        when(apiClientRepository.findById(apiClientId)).thenReturn(null);

        assertThrows(ApiKeyNotFoundException.class, () -> useCase.execute(command));
        verify(apiKeyRepository, times(0)).save(any());
        verify(cache, times(0)).invalidate(any());
    }
}
