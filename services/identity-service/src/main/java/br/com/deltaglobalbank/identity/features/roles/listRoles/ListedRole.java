package br.com.deltaglobalbank.identity.features.roles.listRoles;

public record ListedRole(
    String code,
    String label,
    String description,
    boolean active
) {
}
