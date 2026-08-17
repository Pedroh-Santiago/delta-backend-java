package br.com.deltaglobalbank.customers.features.customers.deleteCustomer;

import java.util.UUID;

public record DeleteCustomerCommand(UUID customerId, UUID tenantId, UUID deletedBy) {
}
