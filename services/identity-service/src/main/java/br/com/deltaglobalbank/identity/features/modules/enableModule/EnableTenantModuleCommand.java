package br.com.deltaglobalbank.identity.features.modules.enableModule;

import java.util.UUID;

public record EnableTenantModuleCommand(UUID tenantId, String moduleCode) {
}
