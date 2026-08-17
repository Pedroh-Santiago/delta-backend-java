package br.com.deltaglobalbank.customers.features.customers.createCustomer

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class CreateCustomerResponse(
    val id: UUID,
    val tenantId: UUID,
    val cpf: String,
    val fullName: String,
    val birthDate: LocalDate,
    val status: String,
    val createdAt: Instant,
)