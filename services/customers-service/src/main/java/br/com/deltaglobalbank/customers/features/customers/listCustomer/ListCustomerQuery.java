package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import java.util.UUID;

public record ListCustomerQuery(UUID tenantId, int page, Integer size) {
}
