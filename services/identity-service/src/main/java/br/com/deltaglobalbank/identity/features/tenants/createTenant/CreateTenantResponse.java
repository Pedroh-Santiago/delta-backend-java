package br.com.deltaglobalbank.identity.features.tenants.createTenant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateTenantResponse(
    UUID id,
    String name,
    String slug,
    String status,
    List<String> enabledModules,
    Instant createdAt
) {
}
