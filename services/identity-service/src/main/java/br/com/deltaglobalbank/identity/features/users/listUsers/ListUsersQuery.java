package br.com.deltaglobalbank.identity.features.users.listUsers;

import java.util.UUID;

public record ListUsersQuery(
    UUID tenantIdFilter,
    int page,
    int size,
    boolean requireTenantExists
) {
    public ListUsersQuery(UUID tenantIdFilter, int page, int size) {
        this(tenantIdFilter, page, size, false);
    }
}
