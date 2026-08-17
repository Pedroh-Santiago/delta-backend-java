package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListModulesUseCase(
    private val moduleRepository: ModuleRepository
) {

    @Transactional(readOnly = true)
    fun execute(): ListModulesResponse {
        val items = moduleRepository.findAll()
            .sortedBy { it.code.value }
            .map { module ->
                ListedModule(
                    code = module.code.value,
                    name = module.name,
                    description = module.description
                )
            }

        return ListModulesResponse(items = items)
    }
}
