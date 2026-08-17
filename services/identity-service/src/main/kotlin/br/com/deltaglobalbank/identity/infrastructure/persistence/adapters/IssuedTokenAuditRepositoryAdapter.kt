package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaIssuedTokenAuditRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IssuedTokenAuditRepositoryAdapter(
    private val jpaIssuedTokenAuditRepository: JpaIssuedTokenAuditRepository
) : IssuedTokenAuditRepository {

    override fun save(audit: IssuedTokenAudit) {
        jpaIssuedTokenAuditRepository.save(audit.toEntity())
    }

    override fun findByJti(jti: String): IssuedTokenAudit? {
       return jpaIssuedTokenAuditRepository.findByJti(UUID.fromString(jti))?.toDomain()

    }
}
