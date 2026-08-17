package br.com.deltaglobalbank.identity.features.modules.disableModule;

import java.util.UUID;

public record DisableTenantModuleCommand(
    UUID tenantId,
    String moduleCode
) {
}
