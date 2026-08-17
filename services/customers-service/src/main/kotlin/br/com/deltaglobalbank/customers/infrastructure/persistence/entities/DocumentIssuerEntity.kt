package br.com.deltaglobalbank.customers.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "document_issuers")
class DocumentIssuerEntity (
    @Id
    var id: UUID,

    @Column(name = "name", nullable = false)
    var name: String,
)

