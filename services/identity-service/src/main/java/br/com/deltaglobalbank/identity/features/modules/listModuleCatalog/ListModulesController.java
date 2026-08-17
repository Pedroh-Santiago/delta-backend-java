package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListModulesController {

    private final ListModulesUseCase listModulesUseCase;

    public ListModulesController(ListModulesUseCase listModulesUseCase) {
        this.listModulesUseCase = listModulesUseCase;
    }

    @GetMapping("/admin/modules")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListModulesResponse> listModules() {
        return ResponseEntity.ok(listModulesUseCase.execute());
    }
}
