package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.RefreshTokenMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRefreshTokenRepository;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository jpaRefreshTokenRepository) {
        this.jpaRefreshTokenRepository = jpaRefreshTokenRepository;
    }

    @Override
    public RefreshToken findById(UUID id) {
        return jpaRefreshTokenRepository.findById(id).map(RefreshTokenMapper::toDomain).orElse(null);
    }

    @Override
    public RefreshToken findByTokenHash(String hash) {
        RefreshTokenEntity entity = jpaRefreshTokenRepository.findByTokenHash(hash);
        return entity != null ? RefreshTokenMapper.toDomain(entity) : null;
    }

    @Override
    public List<RefreshToken> findAllByUserId(UUID userId) {
        return jpaRefreshTokenRepository.findAllByUserId(userId).stream().map(RefreshTokenMapper::toDomain).toList();
    }

    @Override
    public List<RefreshToken> findAllActiveByUserId(UUID userId) {
        return jpaRefreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId).stream()
            .map(RefreshTokenMapper::toDomain)
            .toList();
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity existing = jpaRefreshTokenRepository.findById(refreshToken.getId()).orElse(null);
        RefreshTokenEntity entityToSave = existing != null
            ? RefreshTokenMapper.applyTo(refreshToken, existing)
            : RefreshTokenMapper.toEntity(refreshToken);
        return RefreshTokenMapper.toDomain(jpaRefreshTokenRepository.save(entityToSave));
    }

    @Override
    public List<RefreshToken> saveAll(List<RefreshToken> refreshTokens) {
        List<RefreshTokenEntity> entities = refreshTokens.stream()
            .map(token -> {
                RefreshTokenEntity existing = jpaRefreshTokenRepository.findById(token.getId()).orElse(null);
                return existing != null
                    ? RefreshTokenMapper.applyTo(token, existing)
                    : RefreshTokenMapper.toEntity(token);
            })
            .toList();
        return jpaRefreshTokenRepository.saveAll(entities).stream().map(RefreshTokenMapper::toDomain).toList();
    }
}
