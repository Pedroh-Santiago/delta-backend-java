package br.com.deltaglobalbank.identity.features.apiClients.listClients;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItemsResponse(
    UUID id,
    UUID tenantId,
    String tenantSlug,
    String name,
    String description,
    String status,
    List<String> roles,
    int activeKeysCount,
    Instant createdAt
) {
}
