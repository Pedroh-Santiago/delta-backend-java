package br.com.deltaglobalbank.identity.features.modules.enableModule;

import java.time.Instant;

public record EnableModuleResponse(
    String moduleCode,
    boolean enabled,
    Instant enabledAt
) {
}
