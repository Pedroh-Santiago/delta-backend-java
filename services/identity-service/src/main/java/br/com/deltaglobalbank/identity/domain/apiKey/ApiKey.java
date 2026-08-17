package br.com.deltaglobalbank.identity.domain.apiKey;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.user.HashedPassword;

public class ApiKey {

    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 255;

    private final UUID id;
    private final UUID apiClientId;
    private final String name;
    private final HashedPassword keyHash;
    private final String keyPrefix;
    private final String fingerprint;
    private final Instant createdAt;

    private Instant expiresAt;
    private Instant lastUsedAt;
    private Instant revokedAt;
    private Instant updatedAt;

    private ApiKey(
        UUID id,
        UUID apiClientId,
        String name,
        HashedPassword keyHash,
        String keyPrefix,
        String fingerprint,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.apiClientId = apiClientId;
        this.name = name;
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
        this.fingerprint = fingerprint;
        this.expiresAt = expiresAt;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApiClientId() {
        return apiClientId;
    }

    public String getName() {
        return name;
    }

    public HashedPassword getKeyHash() {
        return keyHash;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    public void revoke() {
        if (isRevoked()) {
            return;
        }
        Instant now = Instant.now();
        revokedAt = now;
        updatedAt = now;
    }

    public void markUsed() {
        Instant now = Instant.now();
        lastUsedAt = now;
        updatedAt = now;
    }

    public Snapshot snapshot() {
        return new Snapshot(
            expiresAt,
            lastUsedAt,
            revokedAt,
            updatedAt
        );
    }

    public record Snapshot(
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt,
        Instant updatedAt
    ) {
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ApiKey apiKey && apiKey.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static ApiKey create(
        UUID id,
        UUID apiClientId,
        String name,
        HashedPassword keyHash,
        String keyPrefix,
        String fingerprint,
        Instant expiresAt
    ) {
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("invalid_api_key_name_length");
        }
        if (keyPrefix.isBlank()) {
            throw new IllegalArgumentException("invalid_key_prefix");
        }
        if (fingerprint.length() != 64) {
            throw new IllegalArgumentException("invalid_fingerprint");
        }
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("expires_at_in_past");
        }

        Instant now = Instant.now();
        return new ApiKey(
            id,
            apiClientId,
            name.trim(),
            keyHash,
            keyPrefix,
            fingerprint,
            expiresAt,
            null,
            null,
            now,
            now
        );
    }

    public static ApiKey restore(
        UUID id,
        UUID apiClientId,
        String name,
        HashedPassword keyHash,
        String keyPrefix,
        String fingerprint,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new ApiKey(
            id,
            apiClientId,
            name,
            keyHash,
            keyPrefix,
            fingerprint,
            expiresAt,
            lastUsedAt,
            revokedAt,
            createdAt,
            updatedAt
        );
    }
}
