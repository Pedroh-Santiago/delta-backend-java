package br.com.deltaglobalbank.identity.features.assignUserRole;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;

public record AssignRolesRequest(
    List<RoleCode> rolesCodes
) {
}
