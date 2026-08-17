package br.com.deltaglobalbank.identity.features.modules.disableModule

import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class DisableTenantModuleCommand(
    val tenantId: UUID,
    val moduleCode: String
)

@Service
class DisableTenantModuleUseCase(
    private val tenantRepository: TenantRepository,
    private val moduleRepository: ModuleRepository,
    private val tenantModuleRepository: TenantModuleRepository
) {

    @Transactional
    fun execute(command: DisableTenantModuleCommand) {
        val tenant = tenantRepository.findById(command.tenantId)
            ?: throw TenantNotFoundException()

        val module = moduleRepository.findByCode(ModuleCode(command.moduleCode.lowercase()))
            ?: throw ModuleNotFoundException()

        val existing = tenantModuleRepository.findByTenantIdAndModuleId(tenant.id, module.id)
            ?: return

        existing.disable()
        tenantModuleRepository.save(existing)
    }
}