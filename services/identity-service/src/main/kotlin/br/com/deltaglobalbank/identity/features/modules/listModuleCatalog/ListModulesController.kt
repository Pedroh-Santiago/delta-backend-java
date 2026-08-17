package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ListModulesController(
    private val listModulesUseCase: ListModulesUseCase
) {

    @GetMapping("/admin/modules")
    @PreAuthorize("hasRole('platform.admin')")
    fun listModules(): ResponseEntity<ListModulesResponse> {
        return ResponseEntity.ok(listModulesUseCase.execute())
    }
}
