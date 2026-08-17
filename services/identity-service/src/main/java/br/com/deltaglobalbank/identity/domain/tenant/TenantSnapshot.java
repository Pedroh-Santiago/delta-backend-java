package br.com.deltaglobalbank.identity.domain.tenant;

import java.time.Instant;
import java.util.UUID;

public record TenantSnapshot(
    UUID id,
    String name,
    String slug,
    TenantStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
