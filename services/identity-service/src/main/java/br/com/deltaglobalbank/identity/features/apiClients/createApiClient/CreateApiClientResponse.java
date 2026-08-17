package br.com.deltaglobalbank.identity.features.apiClients.createApiClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;

public record CreateApiClientResponse(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    String status,
    List<RoleCode> roles,
    Instant createdAt
) {
}
