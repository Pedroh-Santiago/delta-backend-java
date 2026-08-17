package br.com.deltaglobalbank.identity.features.modules.listModules;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListTenantModulesController {

    private final ListTenantModulesUseCase useCase;

    public ListTenantModulesController(ListTenantModulesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/admin/tenants/{tenantId}/modules")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListTenantModulesResponse> list(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(useCase.execute(tenantId));
    }
}
