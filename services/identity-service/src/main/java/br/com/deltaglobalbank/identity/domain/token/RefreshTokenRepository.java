package br.com.deltaglobalbank.identity.domain.token;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken findById(UUID id);

    RefreshToken findByTokenHash(String hash);

    List<RefreshToken> findAllByUserId(UUID userId);

    List<RefreshToken> findAllActiveByUserId(UUID userId);

    RefreshToken save(RefreshToken refreshToken);

    List<RefreshToken> saveAll(List<RefreshToken> refreshTokens);
}
