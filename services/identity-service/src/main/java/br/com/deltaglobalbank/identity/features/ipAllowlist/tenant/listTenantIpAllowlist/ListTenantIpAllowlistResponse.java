package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist;

import java.util.List;

public record ListTenantIpAllowlistResponse(
    List<ListedTenantIpAllowlist> items
) {
}
