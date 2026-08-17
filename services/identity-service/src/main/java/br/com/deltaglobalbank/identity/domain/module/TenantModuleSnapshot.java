package br.com.deltaglobalbank.identity.domain.module;

import java.time.Instant;
import java.util.UUID;

public record TenantModuleSnapshot(
    UUID id,
    UUID tenantId,
    UUID moduleId,
    boolean enabled,
    Instant enabledAt,
    Instant updatedAt,
    Instant deletedAt
) {
}
