package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import java.util.UUID;

public record CreateCustomerCommand(
    UUID tenantId,
    UUID createdBy,
    CreateCustomerRequest request
) {
}
