package br.com.deltaglobalbank.identity.features.modules.enableModule;

import java.util.Locale;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;

@Service
public class EnableTenantModuleUseCase {

    private final TenantRepository tenantRepository;
    private final ModuleRepository moduleRepository;
    private final TenantModuleRepository tenantModuleRepository;

    public EnableTenantModuleUseCase(
        TenantRepository tenantRepository,
        ModuleRepository moduleRepository,
        TenantModuleRepository tenantModuleRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.moduleRepository = moduleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
    }

    public EnableModuleResponse execute(EnableTenantModuleCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId());
        if (tenant == null) {
            throw new TenantNotFoundException();
        }
        Module module = moduleRepository.findByCode(new ModuleCode(command.moduleCode().toLowerCase(Locale.ROOT)));
        if (module == null) {
            throw new ModuleNotFoundException();
        }
        TenantModule existing = tenantModuleRepository.findByTenantIdAndModuleId(tenant.getId(), module.getId());
        TenantModule tenantModule;
        if (existing != null) {
            existing.enable();
            tenantModule = tenantModuleRepository.save(existing);
        } else {
            TenantModule novo = TenantModule.create(UuidCreator.getTimeOrderedEpoch(), tenant.getId(), module.getId());
            tenantModule = tenantModuleRepository.save(novo);
        }
        return new EnableModuleResponse(
            module.getCode().value(),
            tenantModule.isEnabled(),
            tenantModule.snapshot().enabledAt()
        );
    }
}
