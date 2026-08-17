package br.com.deltaglobalbank.identity.features.apiClients.createApiKey;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateApiKeyController {

    private final CreateApiKeyUseCase createApiKeyUseCase;

    public CreateApiKeyController(CreateApiKeyUseCase createApiKeyUseCase) {
        this.createApiKeyUseCase = createApiKeyUseCase;
    }

    @PostMapping("/admin/api-clients/{apiClientId}/keys")
    @PreAuthorize("hasRole('identity.admin')")
    public ResponseEntity<CreateApiKeyResponse> createInOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID apiClientId,
        @Valid @RequestBody CreateApiKeyRequest request
    ) {
        CreateApiKeyCommand command = new CreateApiKeyCommand(
            apiClientId, principal.tenantId(), request.name(), request.expiresAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(createApiKeyUseCase.execute(command));
    }

    @PostMapping("/admin/tenants/{tenantId}/api-clients/{apiClientId}/keys")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<CreateApiKeyResponse> createInSpecificTenant(
        @PathVariable UUID tenantId,
        @PathVariable UUID apiClientId,
        @Valid @RequestBody CreateApiKeyRequest request
    ) {
        CreateApiKeyCommand command = new CreateApiKeyCommand(
            apiClientId, tenantId, request.name(), request.expiresAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(createApiKeyUseCase.execute(command));
    }
}
