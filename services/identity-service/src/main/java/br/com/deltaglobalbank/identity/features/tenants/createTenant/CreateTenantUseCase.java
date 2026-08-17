package br.com.deltaglobalbank.identity.features.tenants.createTenant;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.SlugAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.tenant.TenantSlug;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTenantUseCase {

    private final TenantRepository tenantRepository;
    private final ModuleRepository moduleRepository;
    private final TenantModuleRepository tenantModuleRepository;

    public CreateTenantUseCase(
        TenantRepository tenantRepository,
        ModuleRepository moduleRepository,
        TenantModuleRepository tenantModuleRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.moduleRepository = moduleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
    }

    @Transactional
    public CreateTenantResponse execute(CreateTenantCommand command) {
        TenantSlug tenantSlug = new TenantSlug(command.slug());

        if (tenantRepository.existsBySlug(tenantSlug.value())) {
            throw new SlugAlreadyExistsException();
        }

        Tenant tenant = Tenant.create(UuidCreator.getTimeOrderedEpoch(), command.name(), tenantSlug);

        Tenant savedTenant = tenantRepository.save(tenant);

        List<Module> allModules = moduleRepository.findAll();
        List<String> enabledModuleCodes = allModules.stream()
            .map(module -> {
                TenantModule tenantModule = TenantModule.create(
                    UuidCreator.getTimeOrderedEpoch(), savedTenant.getId(), module.getId());
                tenantModuleRepository.save(tenantModule);
                return module.getCode().value();
            })
            .toList();

        return new CreateTenantResponse(
            savedTenant.getId(),
            savedTenant.getName(),
            savedTenant.getSlug(),
            "active",
            enabledModuleCodes,
            savedTenant.getCreatedAt()
        );
    }
}
