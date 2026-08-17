package br.com.deltaglobalbank.identity.features.modules.listModules;

import java.util.List;

public record ListTenantModulesResponse(
    List<TenantModuleListItem> items
) {
}
