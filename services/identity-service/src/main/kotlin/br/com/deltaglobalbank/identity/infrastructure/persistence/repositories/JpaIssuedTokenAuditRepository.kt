package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaIssuedTokenAuditRepository : JpaRepository<IssuedTokenAuditEntity, UUID> {
    fun findByJti(jti: UUID): IssuedTokenAuditEntity?
}
