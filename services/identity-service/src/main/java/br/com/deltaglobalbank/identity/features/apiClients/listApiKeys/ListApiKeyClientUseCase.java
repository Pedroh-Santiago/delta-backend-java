package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListApiKeyClientUseCase {

    public static final int MAX_PAGE_SIZE = 100;

    private final ApiClientRepository apiClientRepository;
    private final JpaApiKeyRepository jpaApiKeyRepository;

    public ListApiKeyClientUseCase(ApiClientRepository apiClientRepository, JpaApiKeyRepository jpaApiKeyRepository) {
        this.apiClientRepository = apiClientRepository;
        this.jpaApiKeyRepository = jpaApiKeyRepository;
    }

    @Transactional
    public ClientApiKeyResponse getClientApiKey(UUID apiClientId, UUID tenantId, int page, int size) {
        ApiClient apiClient = apiClientRepository.findById(apiClientId);
        if (apiClient == null) {
            throw new ApiClientNotFoundException();
        }
        if (!apiClient.getTenantId().equals(tenantId)) {
            throw new ApiClientNotFoundException();
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.Direction.DESC, "createdAt");

        Page<ApiKeyEntity> apiKeysPage = jpaApiKeyRepository.findByApiClientId(apiClientId, pageable);

        var items = apiKeysPage.getContent().stream()
            .map(key -> new ApiKeyItemsResponse(
                key.getId(),
                key.getName(),
                key.getKeyPrefix(),
                key.getExpiresAt(),
                key.getRevokedAt(),
                key.getLastUsedAt(),
                key.getCreatedAt()
            ))
            .toList();

        return new ClientApiKeyResponse(items);
    }
}
