package br.com.deltaglobalbank.identity.features.modules.disableModule;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DisableTenantModuleController {

    private final DisableTenantModuleUseCase useCase;

    public DisableTenantModuleController(DisableTenantModuleUseCase useCase) {
        this.useCase = useCase;
    }

    @DeleteMapping("/admin/tenants/{tenantId}/modules/{moduleCode}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<Void> disable(@PathVariable UUID tenantId, @PathVariable String moduleCode) {
        useCase.execute(new DisableTenantModuleCommand(tenantId, moduleCode));
        return ResponseEntity.noContent().build();
    }
}
