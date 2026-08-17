package br.com.deltaglobalbank.customers.features.customers.changeCustomerStatus;

import java.util.UUID;

public record ChangeCustomerStatusCommand(
    UUID customerId,
    UUID tenantId,
    UUID updatedBy,
    ChangeCustomerStatusRequest request
) {
}
