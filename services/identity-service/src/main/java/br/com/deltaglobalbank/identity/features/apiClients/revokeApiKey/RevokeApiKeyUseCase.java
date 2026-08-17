package br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey;

import java.time.Instant;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyNotFoundException;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RevokeApiKeyUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyUseCase.class);

    private final ApiKeyRepository apiKeyRepository;
    private final ApiClientRepository apiClientRepository;
    private final ExchangeApiKeyCache cache;

    public RevokeApiKeyUseCase(
        ApiKeyRepository apiKeyRepository,
        ApiClientRepository apiClientRepository,
        ExchangeApiKeyCache cache
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiClientRepository = apiClientRepository;
        this.cache = cache;
    }

    public void execute(RevokeApiKeyCommand command) {
        ApiKey apiKey = apiKeyRepository.findById(command.apiKeyId());
        if (apiKey == null) {
            throw new ApiKeyNotFoundException();
        }

        if (command.tenantId() != null) {
            ApiClient apiClient = apiClientRepository.findById(apiKey.getApiClientId());
            if (apiClient == null) {
                throw new ApiKeyNotFoundException();
            }
            if (!apiClient.getTenantId().equals(command.tenantId())) {
                throw new ApiKeyNotFoundException();
            }
        }

        apiKey.revoke();
        apiKeyRepository.save(apiKey);
        cache.invalidate(apiKey.getFingerprint());

        log.info("api_key revoked apiKeyId={} revokedBy={} at={}",
            apiKey.getId(), command.revokedBy(), Instant.now());
    }
}
