package br.com.deltaglobalbank.identity.domain.user;

import java.time.Instant;
import java.util.UUID;

public record UserSnapshot(
    UUID id,
    UUID tenantId,
    String fullName,
    Email email,
    HashedPassword passwordHash,
    UserStatus status,
    boolean mustChangePassword,
    Instant passwordChangedAt,
    Instant lastLoginAt,
    int failedAttempts,
    Instant lockedUntil,
    Instant createdAt,
    Instant updatedAt
) {
}
