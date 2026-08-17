package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist;

import java.util.UUID;

public record DeleteTenantIpAllowlistCommand(UUID id, UUID tenantId) {
}
