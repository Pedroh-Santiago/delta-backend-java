package br.com.deltaglobalbank.identity.domain.role

import java.util.UUID

interface RoleRepository {
    fun findById(id: UUID): Role?
    fun findByCode(code: RoleCode): Role?
    fun findAll(): List<Role>
    fun findAllByIds(ids: Set<UUID>): List<Role>
    fun findAllByModuleId(moduleId: UUID): List<Role>
    fun findAllByUserId(userId: UUID): List<Role>
    fun findAllByApiClientId(apiClientId: UUID): List<Role>
}
