package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers

import br.com.deltaglobalbank.customers.domain.document.DocumentType
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.PersonalDocumentEntity

fun PersonalDocumentEntity.toDomain(): PersonalDocument = PersonalDocument.restore(
    id = this.id,
    customerId = this.customerId,
    documentType = DocumentType.fromDatabaseValue(this.documentType),
    documentNumber = this.documentNumber,
    issuerId  = this.issuerId,
    issuerState = Uf(this.issuerState),
    issuedAt = this.issuedAt,
    expiresAt = this.expiresAt,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun PersonalDocument.toEntity() = PersonalDocumentEntity(
    id = id,
    customerId = customerId,
    documentType = documentType.toDatabaseValue(),
    documentNumber = documentNumber,
    issuerId = issuerId,
    issuerState = issuerState.value,
    issuedAt = issuedAt,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)