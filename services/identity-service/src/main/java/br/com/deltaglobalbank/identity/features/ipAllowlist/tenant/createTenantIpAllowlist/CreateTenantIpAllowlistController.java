package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CreateTenantIpAllowlistController {

    private final CreateTenantIpAllowlistUsecase createTenantIpAllowlistUsecase;

    public CreateTenantIpAllowlistController(CreateTenantIpAllowlistUsecase createTenantIpAllowlistUsecase) {
        this.createTenantIpAllowlistUsecase = createTenantIpAllowlistUsecase;
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/ip-allowlist")
    public ResponseEntity<CreateTenantIpAllowlistResponse> createForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID tenantId,
        @Valid @RequestBody CreateTenantIpAllowlistRequest request
    ) {
        CreateTenantIpAllowlistCommand command = new CreateTenantIpAllowlistCommand(
            tenantId, request.cidr(), request.description());

        CreateTenantIpAllowlistResponse response = createTenantIpAllowlistUsecase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
