package br.com.deltaglobalbank.identity.features.tenants.listTenants;

import java.time.Instant;
import java.util.UUID;

public record ListedTenant(
    UUID id,
    String name,
    String slug,
    String status,
    Instant createdAt
) {
}
