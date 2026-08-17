package br.com.deltaglobalbank.identity.features.users.listUserRoles;

import java.util.UUID;

public record ListUserRoleQuery(
    UUID tenantId,
    UUID userId
) {
}
