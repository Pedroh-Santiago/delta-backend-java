package br.com.deltaglobalbank.identity.features.roles.updateRoleStatus;

import java.util.UUID;

public record UpdateRoleStatusResponse(
    UUID id,
    String code,
    boolean active
) {
}
