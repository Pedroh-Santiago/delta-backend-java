package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.PersonalDocumentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaPersonalDocumentRepository : JpaRepository<PersonalDocumentEntity, UUID> {
    fun findAllByCustomerId(customerId: UUID): List<PersonalDocumentEntity>
    fun findAllByCustomerIdIn(customerIds: Set<UUID>): List<PersonalDocumentEntity>
}