package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories

import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.MakePixEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import java.util.UUID

@Repository
interface JpaMakePixRepository : JpaRepository<MakePixEntity, UUID> {
    fun findByStatus(status: String, pageable: Pageable): Page<MakePixEntity>
}