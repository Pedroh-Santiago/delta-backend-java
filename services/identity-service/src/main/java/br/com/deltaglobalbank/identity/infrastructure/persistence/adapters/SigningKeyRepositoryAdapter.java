package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.SigningKeyMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository;
import org.springframework.stereotype.Component;

@Component
public class SigningKeyRepositoryAdapter implements SigningKeyRepository {

    private final JpaSigningKeyRepository jpaSigningKeyRepository;

    public SigningKeyRepositoryAdapter(JpaSigningKeyRepository jpaSigningKeyRepository) {
        this.jpaSigningKeyRepository = jpaSigningKeyRepository;
    }

    @Override
    public SigningKey findById(UUID id) {
        return jpaSigningKeyRepository.findById(id).map(SigningKeyMapper::toDomain).orElse(null);
    }

    @Override
    public SigningKey findByKid(String kid) {
        SigningKeyEntity entity = jpaSigningKeyRepository.findByKid(kid);
        return entity != null ? SigningKeyMapper.toDomain(entity) : null;
    }

    @Override
    public List<SigningKey> findAllByStatus(SigningKeyStatus status) {
        return jpaSigningKeyRepository.findAllByStatus(status.toDatabaseValue()).stream()
            .map(SigningKeyMapper::toDomain)
            .toList();
    }

    @Override
    public SigningKey findFirstActive() {
        SigningKeyEntity entity = jpaSigningKeyRepository
            .findFirstByStatusOrderByActivatedAtDesc(SigningKeyStatus.ACTIVE.toDatabaseValue());
        return entity != null ? SigningKeyMapper.toDomain(entity) : null;
    }

    @Override
    public SigningKey save(SigningKey signingKey) {
        SigningKeyEntity existing = jpaSigningKeyRepository.findById(signingKey.getId()).orElse(null);
        SigningKeyEntity entityToSave = existing != null
            ? SigningKeyMapper.applyTo(signingKey, existing)
            : SigningKeyMapper.toEntity(signingKey);
        return SigningKeyMapper.toDomain(jpaSigningKeyRepository.save(entityToSave));
    }
}
