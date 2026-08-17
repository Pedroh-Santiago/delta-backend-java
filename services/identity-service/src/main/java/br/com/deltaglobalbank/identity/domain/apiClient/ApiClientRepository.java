package br.com.deltaglobalbank.identity.domain.apiClient;

import java.util.UUID;

public interface ApiClientRepository {
    ApiClient findById(UUID id);

    ApiClient save(ApiClient apiClient);
}
