package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.applyTo
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SigningKeyRepositoryAdapter(
    private val jpaSigningKeyRepository: JpaSigningKeyRepository
) : SigningKeyRepository {

    override fun findById(id: UUID): SigningKey? =
        jpaSigningKeyRepository.findById(id).orElse(null)?.toDomain()

    override fun findByKid(kid: String): SigningKey? =
        jpaSigningKeyRepository.findByKid(kid)?.toDomain()

    override fun findAllByStatus(status: SigningKeyStatus): List<SigningKey> =
        jpaSigningKeyRepository.findAllByStatus(status.toDatabaseValue()).map { it.toDomain() }

    override fun findFirstActive(): SigningKey? =
        jpaSigningKeyRepository.findFirstByStatusOrderByActivatedAtDesc(SigningKeyStatus.ACTIVE.toDatabaseValue())?.toDomain()

    override fun save(signingKey: SigningKey): SigningKey {
        val existing = jpaSigningKeyRepository.findById(signingKey.id).orElse(null)
        val entityToSave = if (existing != null) signingKey.applyTo(existing) else signingKey.toEntity()
        return jpaSigningKeyRepository.save(entityToSave).toDomain()
    }
}
