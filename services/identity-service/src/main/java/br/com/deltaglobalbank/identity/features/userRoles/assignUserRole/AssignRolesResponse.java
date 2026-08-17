package br.com.deltaglobalbank.identity.features.userRoles.assignUserRole;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;

public record AssignRolesResponse(
    UUID userId,
    List<RoleCode> addedRoles,
    List<RoleCode> currentRoles
) {
}
