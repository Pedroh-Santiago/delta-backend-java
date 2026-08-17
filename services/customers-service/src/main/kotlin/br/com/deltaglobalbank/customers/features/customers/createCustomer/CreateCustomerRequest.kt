package br.com.deltaglobalbank.customers.features.customers.createCustomer

import java.time.LocalDate

data class CreateCustomerRequest(
    val cpf: String,
    val fullName: String,
    val birthDate: LocalDate,
    val gender: String,
    val nationality: String = "brasileira",
    val motherName: String,
    val maritalStatus: String,
    val email: String? = null,
    val phone: PhoneRequest,
    val address: AddressRequest,
    val documents: List<DocumentRequest> = emptyList(),
    val bankAccounts: List<BankAccountRequest> = emptyList(),
)

data class PhoneRequest(
    val phoneNumber: String,
)

data class AddressRequest(
    val cep: String,
    val street: String,
    val number: String? = null,
    val complement: String? = null,
    val neighborhood: String? = null,
    val city: String,
    val state: String,
    val country: String = "BR",
)

data class DocumentRequest(
    val type: String,
    val number: String,
    val issuer: String,
    val issuerState: String,
    val issuedAt: LocalDate,
)

data class BankAccountRequest(
    val bankCode: String,
    val agency: String,
    val accountNumber: String,
    val accountDigit: String? = null,
    val accountType: String,
    val purpose: String,
    val isPrimary: Boolean = false,
)