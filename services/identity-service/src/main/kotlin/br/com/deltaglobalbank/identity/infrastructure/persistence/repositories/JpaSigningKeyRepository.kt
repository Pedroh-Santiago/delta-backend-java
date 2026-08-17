package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaSigningKeyRepository : JpaRepository<SigningKeyEntity, UUID> {
    fun findByKid(kid: String): SigningKeyEntity?
    fun findAllByStatus(status: String): List<SigningKeyEntity>
    fun findFirstByStatusOrderByActivatedAtDesc(status: String): SigningKeyEntity?
}
