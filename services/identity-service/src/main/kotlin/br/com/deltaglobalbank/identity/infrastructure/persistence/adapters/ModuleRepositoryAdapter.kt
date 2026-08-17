package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters

import br.com.deltaglobalbank.identity.domain.module.Module
import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ModuleRepositoryAdapter(
    private val jpaModuleRepository: JpaModuleRepository
) : ModuleRepository {

    override fun findById(id: UUID): Module? =
        jpaModuleRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCode(code: ModuleCode): Module? =
        jpaModuleRepository.findByCode(code.value)?.toDomain()

    override fun findAll(): List<Module> =
        jpaModuleRepository.findAll().map { it.toDomain() }

    override fun findAllByIds(ids: Set<UUID>): List<Module> =
        jpaModuleRepository.findAllById(ids).map { it.toDomain() }
}
