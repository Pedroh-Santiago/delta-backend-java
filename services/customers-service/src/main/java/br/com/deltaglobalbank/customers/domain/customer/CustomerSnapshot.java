package br.com.deltaglobalbank.customers.domain.customer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone;
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument;

public record CustomerSnapshot(
    UUID id,
    UUID tenantId,
    Cpf cpf,
    FullName fullName,
    BirthDate birthDate,
    Gender gender,
    String nationality,
    MotherName motherName,
    MaritalStatus maritalStatus,
    Email email,
    Phone phoneNumber,
    Address address,
    CustomerStatus status,
    Instant createdAt,
    Instant updatedAt,
    UUID createdBy,
    UUID updatedBy,
    List<BankAccount> bankAccounts,
    List<PersonalDocument> documents
) {
}
