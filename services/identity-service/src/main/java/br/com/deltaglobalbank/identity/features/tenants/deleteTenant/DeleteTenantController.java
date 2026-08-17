package br.com.deltaglobalbank.identity.features.tenants.deleteTenant;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteTenantController {

    private final DeleteTenantUseCase deleteTenantUseCase;

    public DeleteTenantController(DeleteTenantUseCase deleteTenantUseCase) {
        this.deleteTenantUseCase = deleteTenantUseCase;
    }

    @PreAuthorize("hasRole('platform.admin')")
    @DeleteMapping("/admin/tenants/{tenantId}")
    public ResponseEntity<Void> deleteTenant(
        @PathVariable UUID tenantId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        deleteTenantUseCase.deleteTenant(tenantId, principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
