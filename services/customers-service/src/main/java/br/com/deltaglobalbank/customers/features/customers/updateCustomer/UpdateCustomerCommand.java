package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import java.util.UUID;

public record UpdateCustomerCommand(
    UUID customerId,
    UUID tenantId,
    UUID updatedBy,
    UpdateCustomerRequest request
) {
}
