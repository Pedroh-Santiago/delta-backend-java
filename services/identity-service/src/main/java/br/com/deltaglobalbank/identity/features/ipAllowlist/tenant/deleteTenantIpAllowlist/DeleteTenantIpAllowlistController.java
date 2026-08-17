package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DeleteTenantIpAllowlistController {

    private final DeleteTenantIpAllowlistUseCase deleteTenantIpAllowlistUseCase;

    public DeleteTenantIpAllowlistController(DeleteTenantIpAllowlistUseCase deleteTenantIpAllowlistUseCase) {
        this.deleteTenantIpAllowlistUseCase = deleteTenantIpAllowlistUseCase;
    }

    @DeleteMapping("/admin/tenants/{tenantId}/ip-allowlist/{id}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<Void> deleteForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("tenantId") UUID tenantId,
        @PathVariable("id") UUID id
    ) {
        DeleteTenantIpAllowlistCommand command = new DeleteTenantIpAllowlistCommand(id, tenantId);
        deleteTenantIpAllowlistUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }
}
