package br.com.deltaglobalbank.identity.features.modules.disableModule;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisableTenantModuleUseCase {

    private final TenantRepository tenantRepository;
    private final ModuleRepository moduleRepository;
    private final TenantModuleRepository tenantModuleRepository;

    public DisableTenantModuleUseCase(
        TenantRepository tenantRepository,
        ModuleRepository moduleRepository,
        TenantModuleRepository tenantModuleRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.moduleRepository = moduleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
    }

    @Transactional
    public void execute(DisableTenantModuleCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId());
        if (tenant == null) {
            throw new TenantNotFoundException();
        }

        Module module = moduleRepository.findByCode(new ModuleCode(command.moduleCode().toLowerCase(Locale.ROOT)));
        if (module == null) {
            throw new ModuleNotFoundException();
        }

        TenantModule existing = tenantModuleRepository.findByTenantIdAndModuleId(tenant.getId(), module.getId());
        if (existing == null) {
            return;
        }

        existing.disable();
        tenantModuleRepository.save(existing);
    }
}
