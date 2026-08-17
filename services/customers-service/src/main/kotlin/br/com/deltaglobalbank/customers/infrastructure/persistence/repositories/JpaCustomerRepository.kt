package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaCustomerRepository : JpaRepository<CustomerEntity, UUID> {
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): CustomerEntity?
    fun findByCpfAndTenantId(cpf: String, tenantId: UUID): CustomerEntity?
    fun existsByCpfAndTenantId(cpf: String, tenantId: UUID): Boolean
    fun findAllByTenantId(tenantId: UUID, pageable: Pageable): Page<CustomerEntity>
    fun findAllByIdInAndTenantId(ids: List<UUID>, tenantId: UUID): List<CustomerEntity>

    @Query("""
    SELECT c FROM CustomerEntity c
    WHERE c.tenantId = :tenantId AND LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
""")
    fun searchByName(@Param("query") query: String, @Param("tenantId") tenantId: UUID, pageable: Pageable): List<CustomerEntity>

}