package br.com.deltaglobalbank.identity.features.modules.listModules;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTenantModulesUseCase {

    private final TenantRepository tenantRepository;
    private final ModuleRepository moduleRepository;
    private final TenantModuleRepository tenantModuleRepository;

    public ListTenantModulesUseCase(
        TenantRepository tenantRepository,
        ModuleRepository moduleRepository,
        TenantModuleRepository tenantModuleRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.moduleRepository = moduleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
    }

    @Transactional(readOnly = true)
    public ListTenantModulesResponse execute(UUID tenantId) {
        if (tenantRepository.findById(tenantId) == null) {
            throw new TenantNotFoundException();
        }

        List<Module> allModules = moduleRepository.findAll();

        Map<UUID, TenantModule> tenantModulesByModuleId = tenantModuleRepository.findAllByTenantId(tenantId).stream()
            .collect(Collectors.toMap(TenantModule::getModuleId, it -> it));

        List<TenantModuleListItem> items = allModules.stream()
            .map(module -> {
                TenantModule tenantModule = tenantModulesByModuleId.get(module.getId());
                return new TenantModuleListItem(
                    module.getCode().value(),
                    module.getName(),
                    tenantModule != null && tenantModule.isEnabled()
                );
            })
            .toList();

        return new ListTenantModulesResponse(items);
    }
}
