package br.com.deltaglobalbank.identity.features.bootstrap;

import java.util.UUID;

public record BootstrapResult(
    UUID tenantId,
    String tenantSlug,
    String adminEmail,
    String temporaryPassword,
    String signingKeyId
) {
}
