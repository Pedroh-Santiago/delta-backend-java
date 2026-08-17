package br.com.deltaglobalbank.identity.features.modules.listModules

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class TenantModuleListItem(
    val moduleCode: String,
    val moduleName: String,
    val enabled: Boolean
)

@Service
class ListTenantModulesUseCase(
    private val tenantRepository: TenantRepository,
    private val moduleRepository: ModuleRepository,
    private val tenantModuleRepository: TenantModuleRepository
) {

    @Transactional(readOnly = true)
    fun execute(tenantId: UUID): ListTenantModulesResponse {
        tenantRepository.findById(tenantId) ?: throw TenantNotFoundException()

        val allModules = moduleRepository.findAll()

        val tenantModulesByModuleId = tenantModuleRepository
            .findAllByTenantId(tenantId)
            .associateBy { it.moduleId }

        val items = allModules.map { module ->
            val tenantModule = tenantModulesByModuleId[module.id]
            TenantModuleListItem(
                moduleCode = module.code.value,
                moduleName = module.name,
                enabled = tenantModule?.isEnabled() ?: false
            )
        }

        return ListTenantModulesResponse(items)
    }
}