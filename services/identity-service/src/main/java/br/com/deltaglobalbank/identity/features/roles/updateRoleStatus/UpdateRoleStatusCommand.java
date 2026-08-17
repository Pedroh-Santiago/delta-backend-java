package br.com.deltaglobalbank.identity.features.roles.updateRoleStatus;

import java.util.UUID;

public record UpdateRoleStatusCommand(
    UUID roleId,
    boolean active
) {
}
