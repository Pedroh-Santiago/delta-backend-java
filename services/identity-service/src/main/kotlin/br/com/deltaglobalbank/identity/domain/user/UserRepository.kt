package br.com.deltaglobalbank.identity.domain.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: Email): User?
    fun save(user: User): User
    fun existsByEmail(email: Email): Boolean
    fun findPage(pageable: Pageable): Page<User>
    fun findPageByTenantId(tenantId: UUID, pageable: Pageable): Page<User>
    fun delete(user: User)
}
