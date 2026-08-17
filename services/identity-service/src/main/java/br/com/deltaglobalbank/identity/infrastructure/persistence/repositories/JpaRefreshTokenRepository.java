package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    RefreshTokenEntity findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findAllByUserId(UUID userId);

    List<RefreshTokenEntity> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}
