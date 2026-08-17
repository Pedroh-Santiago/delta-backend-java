package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListTenantIpAllowlistController {

    private final ListTenantIpAllowlistUseCase listTenantIpAllowlistUseCase;

    public ListTenantIpAllowlistController(ListTenantIpAllowlistUseCase listTenantIpAllowlistUseCase) {
        this.listTenantIpAllowlistUseCase = listTenantIpAllowlistUseCase;
    }

    @GetMapping("/admin/tenant-ip-allowlist")
    @PreAuthorize("hasRole('identity.admin')")
    public ResponseEntity<ListTenantIpAllowlistResponse> listForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ListTenantIpAllowlistQuery query = new ListTenantIpAllowlistQuery(principal.tenantId());
        return ResponseEntity.ok(listTenantIpAllowlistUseCase.execute(query));
    }

    @GetMapping("/admin/tenants/{tenantId}/ip-allowlist")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListTenantIpAllowlistResponse> listForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("tenantId") UUID tenantId
    ) {
        ListTenantIpAllowlistQuery query = new ListTenantIpAllowlistQuery(tenantId);
        return ResponseEntity.ok(listTenantIpAllowlistUseCase.execute(query));
    }
}
