package br.com.deltaglobalbank.identity.features.apiClients.createApiClient;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;

public record CreateApiClientCommand(
    UUID tenantId,
    String name,
    String description,
    List<RoleCode> roleCodes,
    List<String> creatorRoles
) {
}
