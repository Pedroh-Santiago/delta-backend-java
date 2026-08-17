package br.com.deltaglobalbank.customers.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "customer_documents")
@SQLDelete(sql = "UPDATE customers.customer_documents SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class PersonalDocumentEntity(
    @Id
    var id: UUID,

    @Column(name = "customer_id", nullable = false)
    var customerId: UUID,

    @Column(name = "document_type", nullable = false)
    var documentType: String,

    @Column(name = "document_number", nullable = false)
    var documentNumber: String,

    @Column(name = "issuer_id", nullable = false)
    var issuerId: UUID,

    @Column(name = "issuer_state", nullable = false)
    var issuerState: String,

    @Column(name = "issued_at", nullable = false)
    var issuedAt: LocalDate,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDate?,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)

