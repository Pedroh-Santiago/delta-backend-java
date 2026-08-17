package br.com.deltaglobalbank.identity.features.modules.enableModule;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnableTenantModuleController {

    private final EnableTenantModuleUseCase useCase;

    public EnableTenantModuleController(EnableTenantModuleUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/admin/tenants/{tenantId}/modules/{moduleCode}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<EnableModuleResponse> enable(@PathVariable UUID tenantId, @PathVariable String moduleCode) {
        EnableModuleResponse response = useCase.execute(new EnableTenantModuleCommand(tenantId, moduleCode));
        return ResponseEntity.ok(response);
    }
}
