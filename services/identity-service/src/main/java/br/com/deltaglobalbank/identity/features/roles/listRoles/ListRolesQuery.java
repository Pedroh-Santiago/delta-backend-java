package br.com.deltaglobalbank.identity.features.roles.listRoles;

public record ListRolesQuery(
    boolean includePlatformAdmin,
    boolean activeOnly
) {
}
