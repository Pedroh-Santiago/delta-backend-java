package br.com.deltaglobalbank.identity.features.roles.listRoles;

import java.util.List;

public record ListRolesResponse(
    List<ListedRole> items
) {
}
