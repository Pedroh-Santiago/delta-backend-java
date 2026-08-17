package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCustomerResponse(
    UUID id,
    UUID tenantId,
    String cpf,
    String fullName,
    LocalDate birthDate,
    String status,
    Instant createdAt
) {
}
