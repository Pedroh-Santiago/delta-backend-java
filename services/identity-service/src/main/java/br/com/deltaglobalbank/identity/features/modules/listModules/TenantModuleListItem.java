package br.com.deltaglobalbank.identity.features.modules.listModules;

public record TenantModuleListItem(
    String moduleCode,
    String moduleName,
    boolean enabled
) {
}
