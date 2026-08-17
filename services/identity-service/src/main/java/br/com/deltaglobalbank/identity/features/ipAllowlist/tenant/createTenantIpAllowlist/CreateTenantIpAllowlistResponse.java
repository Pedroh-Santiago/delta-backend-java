package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;

public record CreateTenantIpAllowlistResponse(
    UUID id,
    UUID tenantId,
    Cidr cidr,
    String description,
    Instant createdAt
) {
}
