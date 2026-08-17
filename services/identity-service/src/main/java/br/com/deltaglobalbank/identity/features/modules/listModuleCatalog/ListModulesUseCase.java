package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog;

import java.util.Comparator;
import java.util.List;

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListModulesUseCase {

    private final ModuleRepository moduleRepository;

    public ListModulesUseCase(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Transactional(readOnly = true)
    public ListModulesResponse execute() {
        List<ListedModule> items = moduleRepository.findAll().stream()
            .sorted(Comparator.comparing(it -> it.getCode().value()))
            .map(module -> new ListedModule(module.getCode().value(), module.getName(), module.getDescription()))
            .toList();

        return new ListModulesResponse(items);
    }
}
