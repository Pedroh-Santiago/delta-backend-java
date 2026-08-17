package br.com.deltaglobalbank.identity.features.users.removeUserRole;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;

public record RemoveUserRoleCommand(
    UUID tenantId,
    UUID userId,
    RoleCode roleCode,
    AuthenticatedPrincipal principal
) {
}
