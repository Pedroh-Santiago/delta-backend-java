package br.com.deltaglobalbank.identity.domain.tenant

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface TenantRepository {
    fun findById(id: UUID): Tenant?
    fun findBySlug(slug: String): Tenant?
    fun existsBySlug(slug: String): Boolean
    fun findPage(pageable: Pageable): Page<Tenant>
    fun save(tenant: Tenant): Tenant
    fun delete(id: UUID)
}
