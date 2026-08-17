package br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RevokeApiKeyController {

    private final RevokeApiKeyUseCase revokeApiKeyUseCase;

    public RevokeApiKeyController(RevokeApiKeyUseCase revokeApiKeyUseCase) {
        this.revokeApiKeyUseCase = revokeApiKeyUseCase;
    }

    @DeleteMapping("/admin/api-keys/{apiKeyId}")
    @PreAuthorize("hasRole('identity.admin')")
    public ResponseEntity<Void> revokeApiKeyForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("apiKeyId") UUID apiKeyId
    ) {
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(apiKeyId, principal.tenantId(), principal.subject());
        revokeApiKeyUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/tenants/{tenantId}/api-keys/{apiKeyId}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<Void> revokeApiKeyForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("tenantId") UUID tenantId,
        @PathVariable("apiKeyId") UUID apiKeyId
    ) {
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(apiKeyId, tenantId, principal.subject());
        revokeApiKeyUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }
}
