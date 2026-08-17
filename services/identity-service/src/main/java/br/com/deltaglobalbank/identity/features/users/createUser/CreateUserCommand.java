package br.com.deltaglobalbank.identity.features.users.createUser;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;

public record CreateUserCommand(
    UUID tenantId,
    String fullName,
    String email,
    List<RoleCode> roleCodes,
    UUID grantedBy,
    List<String> creatorRoles
) {
}
