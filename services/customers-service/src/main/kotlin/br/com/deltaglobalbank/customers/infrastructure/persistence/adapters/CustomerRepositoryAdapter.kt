package br.com.deltaglobalbank.customers.infrastructure.persistence.adapters

import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaPersonalDocumentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CustomerRepositoryAdapter(
    private val jpaBankAccount: JpaBankAccountRepository,
    private val jpaDocument: JpaPersonalDocumentRepository,
    private val jpaCustomer: JpaCustomerRepository,
): CustomerRepository{

    private fun CustomerEntity.toDomainWithChildren(): Customer {
        val accounts = jpaBankAccount.findAllByCustomerId(id).map { it.toDomain() }
        val documents = jpaDocument.findAllByCustomerId(id).map { it.toDomain() }
        return toDomain(accounts, documents)   // o toDomain(lists) da Fase 7
    }

    @Transactional
    override fun save(customer: Customer): Customer {
        val existing = jpaCustomer.findById(customer.id).orElse(null)
        val entity = if (existing != null) customer.applyTo(existing) else customer.toEntity()
        jpaCustomer.save(entity)

        syncBankAccounts(customer)
        syncDocuments(customer)

        return findById(customer.id, customer.tenantId)!!
    }

    override fun findById(id: UUID, tenantId: UUID): Customer? =
        jpaCustomer.findByIdAndTenantId(id, tenantId)?.toDomainWithChildren()

    override fun findByCpfAndTenantId(cpf: Cpf, tenantId: UUID): Customer? =
        jpaCustomer.findByCpfAndTenantId(cpf.value, tenantId)?.toDomainWithChildren()   // ← cpf.value

    override fun existsByCpfAndTenantId(cpf: Cpf, tenantId: UUID): Boolean =
        jpaCustomer.existsByCpfAndTenantId(cpf.value, tenantId)

    override fun findPage(tenantId: UUID, pageable: Pageable): Page<Customer> =
        jpaCustomer.findAllByTenantId(tenantId, pageable).map { it.toDomainWithChildren() }

    override fun findAllByIds(ids: List<UUID>, tenantId: UUID): List<Customer> =
        assemble(jpaCustomer.findAllByIdInAndTenantId(ids, tenantId))

    override fun searchByName(query: String, tenantId: UUID, limit: Int): List<Customer> =
        assemble(jpaCustomer.searchByName(query, tenantId, PageRequest.of(0, limit)))

    @Transactional
    override fun delete(customer: Customer) {
        jpaBankAccount.deleteAll(jpaBankAccount.findAllByCustomerId(customer.id))
        jpaDocument.deleteAll(jpaDocument.findAllByCustomerId(customer.id))
        jpaCustomer.deleteById(customer.id)
    }

    private fun syncBankAccounts(customer: Customer) {
        val existing = jpaBankAccount.findAllByCustomerId(customer.id)
        val currentIds = customer.bankAccounts.map { it.id }.toSet()

        jpaBankAccount.saveAll(customer.bankAccounts.map { it.toEntity() })
        jpaBankAccount.deleteAll(existing.filter { it.id !in currentIds })
    }

    private fun syncDocuments(customer: Customer) {
        val existing = jpaDocument.findAllByCustomerId(customer.id)
        val currentIds = customer.documents.map { it.id }.toSet()

        jpaDocument.saveAll(customer.documents.map { it.toEntity() })
        jpaDocument.deleteAll(existing.filter { it.id !in currentIds })
    }

    private fun assemble(entities: List<CustomerEntity>): List<Customer> {
        if (entities.isEmpty()) return emptyList()
        val ids = entities.map { it.id }.toSet()
        val accts = jpaBankAccount.findAllByCustomerIdIn(ids).groupBy { it.customerId }
        val docs = jpaDocument.findAllByCustomerIdIn(ids).groupBy { it.customerId }
        return entities.map { it.toDomain(accts[it.id].orEmpty().map { a -> a.toDomain() }, docs[it.id].orEmpty().map { d -> d.toDomain() }) }
    }

}

