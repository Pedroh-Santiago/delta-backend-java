package br.com.deltaglobalbank.identity.features.users.listUserRoles;

import java.util.List;

public record ListUserRolesResponse(
    List<ListedUserRoles> items
) {
}
