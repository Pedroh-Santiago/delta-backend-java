package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
    @NotBlank String cpf,
    @NotBlank String fullName,
    @NotNull LocalDate birthDate,
    @NotBlank String gender,
    String nationality,
    @NotBlank String motherName,
    @NotBlank String maritalStatus,
    String email,
    @NotNull @Valid PhoneRequest phone,
    @NotNull @Valid AddressRequest address,
    @Valid List<DocumentRequest> documents,
    @Valid List<BankAccountRequest> bankAccounts
) {
    public CreateCustomerRequest {
        if (nationality == null) {
            nationality = "brasileira";
        }
        if (documents == null) {
            documents = List.of();
        }
        if (bankAccounts == null) {
            bankAccounts = List.of();
        }
    }
}
