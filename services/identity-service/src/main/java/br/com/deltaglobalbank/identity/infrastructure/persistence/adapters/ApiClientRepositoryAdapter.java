package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.ApiClientMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository;
import org.springframework.stereotype.Component;

@Component
public class ApiClientRepositoryAdapter implements ApiClientRepository {

    private final JpaApiClientRepository jpaApiClientRepository;

    public ApiClientRepositoryAdapter(JpaApiClientRepository jpaApiClientRepository) {
        this.jpaApiClientRepository = jpaApiClientRepository;
    }

    @Override
    public ApiClient findById(UUID id) {
        return jpaApiClientRepository.findById(id).map(ApiClientMapper::toDomain).orElse(null);
    }

    @Override
    public ApiClient save(ApiClient apiClient) {
        ApiClientEntity existing = jpaApiClientRepository.findById(apiClient.getId()).orElse(null);
        ApiClientEntity toSave = existing != null
            ? ApiClientMapper.applyTo(apiClient, existing)
            : ApiClientMapper.toEntity(apiClient);
        return ApiClientMapper.toDomain(jpaApiClientRepository.save(toSave));
    }
}
