package br.com.deltaglobalbank.identity.features.modules.enableModule

import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EnableTenantModuleUseCase(
    val tenantRepository: TenantRepository,
    val moduleRepository: ModuleRepository,
    val tenantModuleRepository: TenantModuleRepository
) {

    fun execute(command: EnableTenantModuleCommand): EnableModuleResponse {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: throw TenantNotFoundException()
        val module = moduleRepository.findByCode(ModuleCode(command.moduleCode.lowercase()))
            ?: throw ModuleNotFoundException()
        val existing = tenantModuleRepository.findByTenantIdAndModuleId(tenant.id, module.id)
        val tenantModule = if (existing != null) {
            existing.enable()
            tenantModuleRepository.save(existing)
        } else {
            val novo = TenantModule.create(
                id = UuidCreator.getTimeOrderedEpoch(),
                tenantId = tenant.id,
                moduleId = module.id
            )
            tenantModuleRepository.save(novo)
        }
        return EnableModuleResponse(
            module.code.value,
            tenantModule.isEnabled(),
            tenantModule.snapshot().enabledAt
        )
    }

}

data class EnableTenantModuleCommand(val tenantId: UUID, val moduleCode: String) {

}
