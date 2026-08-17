package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.user.Email
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserRepositoryAdapter(private val jpaUserRepository: JpaUserRepository) : UserRepository {

    override fun findById(id: UUID): User? =
        jpaUserRepository.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: Email): User? =
        jpaUserRepository.findByEmail(email.value)?.toDomain()

    override fun existsByEmail(email: Email): Boolean =
        jpaUserRepository.existsByEmail(email.value)

    override fun save(user: User): User {
        val existing = jpaUserRepository.findById(user.id).orElse(null)
        val entityToSave = if (existing != null) user.applyTo(existing) else user.toEntity()
        return jpaUserRepository.save(entityToSave).toDomain()
    }

    override fun findPage(pageable: Pageable): Page<User> {
        return jpaUserRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun findPageByTenantId(tenantId: UUID, pageable: Pageable): Page<User> {
        return jpaUserRepository.findAllByTenantId(tenantId, pageable).map { it.toDomain() }
    }

    override fun delete(user: User) {
        jpaUserRepository.deleteById(user.id)
    }
}
