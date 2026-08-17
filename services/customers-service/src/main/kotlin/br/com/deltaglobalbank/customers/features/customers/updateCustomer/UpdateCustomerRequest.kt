package br.com.deltaglobalbank.customers.features.customers.updateCustomer

import java.time.LocalDate
import java.util.UUID

data class UpdateCustomerRequest(
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
    val phoneNumber: String
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
    val id: UUID? = null,
    val type: String, val number: String, val issuer: String,
    val issuerState: String, val issuedAt: LocalDate,
)

data class BankAccountRequest(
    val id: UUID? = null,
    val bankCode: String, val agency: String, val accountNumber: String,
    val accountDigit: String? = null, val accountType: String,
    val purpose: String, val isPrimary: Boolean = false,
)