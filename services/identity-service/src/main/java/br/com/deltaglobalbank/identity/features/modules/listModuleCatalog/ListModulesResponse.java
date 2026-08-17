package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog;

import java.util.List;

public record ListModulesResponse(
    List<ListedModule> items
) {
}
