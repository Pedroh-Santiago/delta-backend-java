package br.com.deltaglobalbank.identity.features.users.listUserRoles;

import java.time.Instant;
import java.util.UUID;

public record ListedUserRoles(
    String roleCode,
    UUID roleId,
    String moduleCode,
    Instant grantedAt,
    UUID grantedBy
) {
}
