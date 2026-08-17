package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.BankAccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaBankAccountRepository : JpaRepository<BankAccountEntity, UUID> {
    fun findAllByCustomerId(customerId: UUID): List<BankAccountEntity>
    fun findAllByCustomerIdIn(customerIds: Set<UUID>): List<BankAccountEntity>
}