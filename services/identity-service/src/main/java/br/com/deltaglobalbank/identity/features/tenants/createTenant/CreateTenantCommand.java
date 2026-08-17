package br.com.deltaglobalbank.identity.features.tenants.createTenant;

public record CreateTenantCommand(
    String name,
    String slug
) {
}
