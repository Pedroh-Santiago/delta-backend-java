package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import java.util.UUID;

public record ListCustomerQuery(
    UUID tenantId,
    int page,
    Integer size,
    String status,
    String cpf,
    String fullName
) {
    public ListCustomerQuery(UUID tenantId, int page, Integer size) {
        this(tenantId, page, size, null, null, null);
    }
}
