package br.com.deltaglobalbank.customers.features.customers.getCustomer

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class GetCustomerResponse(
    val id: UUID,
    val tenantId: UUID,
    val cpf: String,
    val fullName: String,
    val birthDate: LocalDate,
    val gender: String,
    val nationality: String,
    val motherName: String,
    val maritalStatus: String,
    val email: String?,
    val phone: PhoneResponse,
    val address: AddressResponse,
    val status: String,
    val documents: List<DocumentResponse>,
    val bankAccounts: List<BankAccountResponse>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class PhoneResponse(
    val phoneNumber: String,
)

data class AddressResponse(
    val cep: String,
    val street: String,
    val number: String? = null,
    val complement: String? = null,
    val neighborhood: String? = null,
    val city: String,
    val state: String,
    val country: String = "BR",
)

data class DocumentResponse(
    val id: UUID,
    val type: String,
    val number: String,
    val issuer: String,
    val issuerState: String,
    val issuedAt: LocalDate,
    val expiresAt: LocalDate?
)

data class BankAccountResponse(
    val id: UUID,
    val bankCode: String,
    val agency: String,
    val accountNumber: String,
    val accountDigit: String? = null,
    val accountType: String,
    val purpose: String,
    val isPrimary: Boolean = false,
)