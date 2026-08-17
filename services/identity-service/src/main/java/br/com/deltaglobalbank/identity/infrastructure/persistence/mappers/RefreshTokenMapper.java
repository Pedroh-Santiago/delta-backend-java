package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity;

public final class RefreshTokenMapper {

    private RefreshTokenMapper() {
    }

    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(
            entity.getId(),
            entity.getUserId(),
            entity.getTokenHash(),
            entity.getExpiresAt(),
            entity.getRevokedAt(),
            entity.getCreatedAt(),
            entity.getLastUsedAt(),
            entity.getUserAgent(),
            entity.getIpAddress()
        );
    }

    public static RefreshTokenEntity toEntity(RefreshToken refreshToken) {
        RefreshTokenSnapshot s = refreshToken.snapshot();
        return new RefreshTokenEntity(
            s.id(),
            s.userId(),
            s.tokenHash(),
            s.expiresAt(),
            s.revokedAt(),
            s.createdAt(),
            s.lastUsedAt(),
            s.userAgent(),
            s.ipAddress()
        );
    }

    public static RefreshTokenEntity applyTo(RefreshToken refreshToken, RefreshTokenEntity entity) {
        RefreshTokenSnapshot s = refreshToken.snapshot();
        entity.setRevokedAt(s.revokedAt());
        entity.setLastUsedAt(s.lastUsedAt());
        return entity;
    }
}
