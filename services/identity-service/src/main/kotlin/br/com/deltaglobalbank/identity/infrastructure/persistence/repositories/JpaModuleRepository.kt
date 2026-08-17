package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaModuleRepository : JpaRepository<ModuleEntity, UUID> {
    fun findByCode(code: String): ModuleEntity?
}
