package br.com.deltaglobalbank.identity.domain.token;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;
    private final String userAgent;
    private final String ipAddress;

    private Instant revokedAt;
    private Instant lastUsedAt;

    public RefreshToken(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt,
        Instant createdAt,
        Instant lastUsedAt,
        String userAgent,
        String ipAddress
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public static RefreshToken newToken(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant expiresAt,
        String userAgent,
        String ipAddress
    ) {
        return new RefreshToken(
            id,
            userId,
            tokenHash,
            expiresAt,
            null,
            Instant.now(),
            null,
            userAgent,
            ipAddress
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void revoke() {
        revokedAt = Instant.now();
    }

    public void markUsed() {
        lastUsedAt = Instant.now();
    }

    public boolean isActive() {
        return revokedAt == null && !isExpired();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public RefreshTokenSnapshot snapshot() {
        return new RefreshTokenSnapshot(
            id,
            userId,
            tokenHash,
            expiresAt,
            revokedAt,
            createdAt,
            lastUsedAt,
            userAgent,
            ipAddress
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof RefreshToken refreshToken && id.equals(refreshToken.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RefreshToken(id=" + id + ", userId=" + userId + ")";
    }
}
