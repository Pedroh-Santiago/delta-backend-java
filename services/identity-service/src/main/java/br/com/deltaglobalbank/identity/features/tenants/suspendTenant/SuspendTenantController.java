package br.com.deltaglobalbank.identity.features.tenants.suspendTenant;

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
public class SuspendTenantController {

    private final SuspendTenantUseCase suspendTenantUseCase;

    public SuspendTenantController(SuspendTenantUseCase suspendTenantUseCase) {
        this.suspendTenantUseCase = suspendTenantUseCase;
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/suspend")
    public ResponseEntity<Void> suspendTenant(
        @PathVariable UUID tenantId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        suspendTenantUseCase.suspendTenant(tenantId, principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
