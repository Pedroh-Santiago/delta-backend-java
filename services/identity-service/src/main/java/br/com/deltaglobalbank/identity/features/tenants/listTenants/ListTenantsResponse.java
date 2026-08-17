package br.com.deltaglobalbank.identity.features.tenants.listTenants;

import java.util.List;

public record ListTenantsResponse(
    List<ListedTenant> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
