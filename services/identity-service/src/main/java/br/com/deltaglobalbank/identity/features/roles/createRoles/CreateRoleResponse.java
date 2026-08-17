package br.com.deltaglobalbank.identity.features.roles.createRoles;

import java.time.Instant;
import java.util.UUID;

public record CreateRoleResponse(
    UUID id,
    String code,
    String label,
    UUID moduleId,
    String description,
    boolean active,
    Instant createdAt
) {
}
