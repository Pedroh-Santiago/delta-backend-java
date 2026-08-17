package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist;

import java.util.UUID;

public record CreateTenantIpAllowlistCommand(
    UUID tenantId,
    String cidr,
    String description
) {
}
