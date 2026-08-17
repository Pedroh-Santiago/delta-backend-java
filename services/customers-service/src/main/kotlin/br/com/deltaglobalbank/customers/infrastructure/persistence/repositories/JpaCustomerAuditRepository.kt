package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaCustomerAuditRepository : JpaRepository<CustomerAuditEntity, UUID>