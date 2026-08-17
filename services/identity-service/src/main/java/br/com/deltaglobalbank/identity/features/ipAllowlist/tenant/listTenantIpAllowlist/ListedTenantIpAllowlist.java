package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;

public record ListedTenantIpAllowlist(
    UUID id,
    UUID tenantId,
    Cidr cidr,
    String description,
    Instant createdAt
) {
}
