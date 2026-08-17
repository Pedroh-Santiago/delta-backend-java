package br.com.deltaglobalbank.identity.features.roles.createRoles;

import java.util.UUID;

public record CreateRoleCommand(
    String code,
    String label,
    UUID moduleId,
    String description
) {
}
