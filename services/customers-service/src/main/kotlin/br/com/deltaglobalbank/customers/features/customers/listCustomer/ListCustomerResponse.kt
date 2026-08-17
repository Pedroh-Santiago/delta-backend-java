package br.com.deltaglobalbank.customers.features.customers.listCustomer

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ListCustomerResponse (
    val items: List<ListedCustomer>,
    val page: Int,
    val size: Int?,
    val totalElements: Long,
    val totalPages: Int
)

data class ListedCustomer(
    val id: UUID,
    val cpf: String,
    val fullName: String,
    val birthDate: LocalDate,
    val status: String,
    val createdAt: Instant
)