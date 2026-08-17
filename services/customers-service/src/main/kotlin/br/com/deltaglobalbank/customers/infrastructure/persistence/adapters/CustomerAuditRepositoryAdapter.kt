package br.com.deltaglobalbank.customers.infrastructure.persistence.adapters

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerAuditRepository
import org.springframework.stereotype.Component

@Component
class CustomerAuditRepositoryAdapter(
    private val jpa: JpaCustomerAuditRepository,
) : CustomerAuditRepository {
    override fun saveAll(entries: List<CustomerAuditEntry>) {
        jpa.saveAll(entries.map { it.toEntity() })
    }
}