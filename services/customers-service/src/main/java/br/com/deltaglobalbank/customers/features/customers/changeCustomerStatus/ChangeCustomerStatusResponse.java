package br.com.deltaglobalbank.customers.features.customers.changeCustomerStatus;

import java.time.Instant;
import java.util.UUID;

public record ChangeCustomerStatusResponse(
    UUID id,
    String status,
    Instant updatedAt,
    UUID updatedBy
) {
}
