package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientApikeyController {

    private final ListApiKeyClientUseCase listApiKeyClientUseCase;

    public ClientApikeyController(ListApiKeyClientUseCase listApiKeyClientUseCase) {
        this.listApiKeyClientUseCase = listApiKeyClientUseCase;
    }

    @PreAuthorize("hasRole('identity.admin')")
    @GetMapping("/admin/api-clients/{apiClientId}/keys")
    public ResponseEntity<ClientApiKeyResponse> listClientApiKey(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID apiClientId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        ClientApiKeyResponse response = listApiKeyClientUseCase.getClientApiKey(
            apiClientId, principal.tenantId(), page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @GetMapping("/admin/tenants/{tenantId}/api-clients/{apiClientId}/keys")
    public ResponseEntity<ClientApiKeyResponse> listClientApiKeyByTenantId(
        @PathVariable UUID apiClientId,
        @PathVariable UUID tenantId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        ClientApiKeyResponse response = listApiKeyClientUseCase.getClientApiKey(apiClientId, tenantId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
