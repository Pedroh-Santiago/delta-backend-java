package br.com.deltaglobalbank.customers.domain.document

import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class PersonalDocument private constructor(
    val id: UUID,
    val customerId: UUID,
    val documentType: DocumentType,
    val documentNumber: String,
    val issuerId: UUID,
    val issuerState: Uf,
    val expiresAt: LocalDate?,
    val issuedAt: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant
){
    companion object {
        fun create(
            id: UUID,
            customerId: UUID,
            documentType: DocumentType,
            documentNumber: String,
            issuerId: UUID,
            issuerState: Uf,
            expiresAt: LocalDate?,
            issuedAt: LocalDate,
        ): PersonalDocument {
            val now = Instant.now()
            return PersonalDocument(
                id = id,
                customerId = customerId,
                documentType = documentType,
                documentNumber = documentNumber,
                issuerId  = issuerId,
                issuerState = issuerState,
                expiresAt = expiresAt,
                issuedAt = issuedAt,
                createdAt = now,
                updatedAt = now
            )
        }

        fun restore(
            id: UUID,
            customerId: UUID,
            documentType: DocumentType,
            documentNumber: String,
            issuerId: UUID,
            issuerState: Uf,
            expiresAt: LocalDate?,
            issuedAt: LocalDate,
            createdAt: Instant,
            updatedAt: Instant
        ): PersonalDocument = PersonalDocument(
            id = id,
            customerId = customerId,
            documentType = documentType,
            documentNumber = documentNumber,
            issuerId = issuerId,
            issuerState = issuerState,
            expiresAt = expiresAt,
            issuedAt = issuedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    override fun equals(other: Any?) = other is PersonalDocument && other.id == id

    override fun hashCode() = id.hashCode()
}