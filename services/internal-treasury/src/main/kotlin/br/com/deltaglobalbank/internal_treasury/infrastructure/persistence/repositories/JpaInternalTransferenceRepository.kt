package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories


import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.InternalTransferenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import java.util.UUID

interface JpaInternalTransferenceRepository : JpaRepository<InternalTransferenceEntity, UUID> {
    fun findByStatus(status: String, pageable: Pageable): Page<InternalTransferenceEntity>
}