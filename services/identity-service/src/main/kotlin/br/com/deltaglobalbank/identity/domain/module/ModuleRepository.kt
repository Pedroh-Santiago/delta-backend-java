package br.com.deltaglobalbank.identity.domain.module

import java.util.UUID

interface ModuleRepository {
    fun findById(id: UUID): Module?
    fun findByCode(code: ModuleCode): Module?
    fun findAll(): List<Module>
    fun findAllByIds(ids: Set<UUID>): List<Module>
}
