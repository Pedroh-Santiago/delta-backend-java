package br.com.deltaglobalbank.customers.domain.customer

import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CustomerRepository {
    fun save(customer: Customer): Customer
    fun findById(id: UUID, tenantId: UUID): Customer?
    fun findByCpfAndTenantId(cpf: Cpf, tenantId: UUID): Customer?
    fun existsByCpfAndTenantId(cpf: Cpf, tenantId: UUID): Boolean
    fun findPage(tenantId: UUID, pageable: Pageable): Page<Customer>
    fun findAllByIds(ids: List<UUID>, tenantId: UUID): List<Customer>
    fun searchByName(query: String, tenantId: UUID, limit: Int): List<Customer>
    fun delete(customer: Customer)
}