package br.com.deltaglobalbank.identity.features.tenants.activateTenant;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivateTenantController {

    private final ActivateTenantUseCase activateTenantUseCase;

    public ActivateTenantController(ActivateTenantUseCase activateTenantUseCase) {
        this.activateTenantUseCase = activateTenantUseCase;
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/activate")
    public ResponseEntity<Void> activateTenant(
        @PathVariable UUID tenantId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        activateTenantUseCase.activateTenant(tenantId, principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
