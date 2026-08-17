package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GetCustomerResponse(
    UUID id,
    UUID tenantId,
    String cpf,
    String fullName,
    LocalDate birthDate,
    String gender,
    String nationality,
    String motherName,
    String maritalStatus,
    String email,
    PhoneResponse phone,
    AddressResponse address,
    String status,
    List<DocumentResponse> documents,
    List<BankAccountResponse> bankAccounts,
    Instant createdAt,
    Instant updatedAt
) {
}
