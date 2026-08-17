package br.com.deltaglobalbank.identity.features.apiClients.listClients;

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
public class ListClientsController {

    private final ListClientsUseCase listClientsUseCase;

    public ListClientsController(ListClientsUseCase listClientsUseCase) {
        this.listClientsUseCase = listClientsUseCase;
    }

    @PreAuthorize("hasRole('identity.admin')")
    @GetMapping("admin/api-clients")
    public ResponseEntity<ListClientsResponse> listClients(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        ListClientsResponse response = listClientsUseCase.listClients(principal.tenantId(), page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @GetMapping("admin/tenants/{tenantId}/api-clients")
    public ResponseEntity<ListClientsResponse> listClientsByTenantId(
        @PathVariable UUID tenantId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        ListClientsResponse response = listClientsUseCase.listClients(tenantId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
