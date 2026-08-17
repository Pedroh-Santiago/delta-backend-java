package br.com.deltaglobalbank.identity.features.users.listUsers;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListUsersController {

    private final ListUsersUseCase listUsersUseCase;

    public ListUsersController(ListUsersUseCase listUsersUseCase) {
        this.listUsersUseCase = listUsersUseCase;
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('platform.admin') or hasRole('identity.admin')")
    public ResponseEntity<ListUsersResponse> listUsers(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        boolean isPlatformAdmin = principal.roles().contains("platform.admin");

        ListUsersQuery query = new ListUsersQuery(
            isPlatformAdmin ? null : principal.tenantId(), page, size, false);

        return ResponseEntity.ok(listUsersUseCase.execute(query));
    }

    @GetMapping("/admin/tenants/{tenantId}/users")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListUsersResponse> listUsersByTenant(
        @PathVariable UUID tenantId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        ListUsersQuery query = new ListUsersQuery(tenantId, page, size, true);
        return ResponseEntity.ok(listUsersUseCase.execute(query));
    }
}
