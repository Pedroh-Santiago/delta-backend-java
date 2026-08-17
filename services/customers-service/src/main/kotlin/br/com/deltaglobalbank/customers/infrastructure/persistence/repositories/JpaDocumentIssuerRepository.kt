package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.DocumentIssuerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaDocumentIssuerRepository : JpaRepository<DocumentIssuerEntity, UUID> {
    fun findByName(name: String): DocumentIssuerEntity?
}