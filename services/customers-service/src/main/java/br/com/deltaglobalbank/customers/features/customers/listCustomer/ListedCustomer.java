package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ListedCustomer(
    UUID id,
    String cpf,
    String fullName,
    LocalDate birthDate,
    String phoneNumber,
    String city,
    String state,
    String status,
    Instant createdAt
) {
}
