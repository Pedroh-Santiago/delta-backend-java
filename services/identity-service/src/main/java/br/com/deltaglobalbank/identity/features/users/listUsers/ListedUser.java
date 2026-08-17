package br.com.deltaglobalbank.identity.features.users.listUsers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListedUser(
    UUID id,
    String fullName,
    String email,
    UUID tenantId,
    String tenantSlug,
    String status,
    List<String> roles,
    boolean mustChangePassword,
    int failedAttempts,
    Instant lockedUntil,
    Instant lastLoginAt,
    Instant createdAt
) {
}
