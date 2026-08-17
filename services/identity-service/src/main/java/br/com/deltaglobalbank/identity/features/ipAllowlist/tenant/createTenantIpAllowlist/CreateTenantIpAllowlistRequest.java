package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantIpAllowlistRequest(
    @NotBlank String cidr,
    @Size(max = 255) String description
) {
}
