package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.ApiKeyMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

    private final JpaApiKeyRepository jpaRepository;

    public ApiKeyRepositoryAdapter(JpaApiKeyRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        ApiKeyEntity existing = jpaRepository.findById(apiKey.getId()).orElse(null);
        if (existing == null) {
            return ApiKeyMapper.toDomain(jpaRepository.save(ApiKeyMapper.toEntity(apiKey)));
        }
        ApiKeyMapper.applyTo(apiKey, existing);
        return ApiKeyMapper.toDomain(jpaRepository.save(existing));
    }

    @Override
    public ApiKey findById(UUID id) {
        return jpaRepository.findById(id).map(ApiKeyMapper::toDomain).orElse(null);
    }

    @Override
    public ApiKey findByFingerprint(String fingerprint) {
        ApiKeyEntity entity = jpaRepository.findByFingerprint(fingerprint);
        return entity != null ? ApiKeyMapper.toDomain(entity) : null;
    }
}
