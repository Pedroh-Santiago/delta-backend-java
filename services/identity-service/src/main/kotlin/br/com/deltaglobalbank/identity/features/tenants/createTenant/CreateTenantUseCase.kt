package br.com.deltaglobalbank.identity.features.tenants.createTenant

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.tenant.SlugAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantSlug
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreateTenantCommand(
    val name: String,
    val slug: String
)

@Service
class CreateTenantUseCase(
    private val tenantRepository: TenantRepository,
    private val moduleRepository: ModuleRepository,
    private val tenantModuleRepository: TenantModuleRepository
) {

    @Transactional
    fun execute(command: CreateTenantCommand): CreateTenantResponse {
        val tenantSlug = TenantSlug(command.slug)

        if (tenantRepository.existsBySlug(tenantSlug.value)) {
            throw SlugAlreadyExistsException()
        }

        val tenant = Tenant.create(
            id = UuidCreator.getTimeOrderedEpoch(),
            name = command.name,
            slug = tenantSlug
        )

        val savedTenant = tenantRepository.save(tenant)

        val allModules = moduleRepository.findAll()
        val enabledModuleCodes = allModules.map { module ->
            val tenantModule = TenantModule.create(
                id = UuidCreator.getTimeOrderedEpoch(),
                tenantId = savedTenant.id,
                moduleId = module.id
            )
            tenantModuleRepository.save(tenantModule)
            module.code.value
        }

        return CreateTenantResponse(
            id = savedTenant.id,
            name = savedTenant.name,
            slug = savedTenant.slug,
            status = "active",
            enabledModules = enabledModuleCodes,
            createdAt = savedTenant.createdAt
        )
    }
}