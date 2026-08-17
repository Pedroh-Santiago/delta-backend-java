package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import java.util.UUID;

public record GetCustomerRequest(UUID customerId, UUID tenantId) {
}
