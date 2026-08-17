package br.com.deltaglobalbank.identity.features.users.listUserRoles;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ListUserRolesController {

    private final ListUserRoleUseCase listUserRoleUseCase;

    public ListUserRolesController(ListUserRoleUseCase listUserRoleUseCase) {
        this.listUserRoleUseCase = listUserRoleUseCase;
    }

    @GetMapping("/admin/users/{userId}/roles")
    @PreAuthorize("hasRole('identity.admin')")
    public ResponseEntity<ListUserRolesResponse> listUserRolesForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("userId") UUID userId
    ) {
        ListUserRoleQuery query = new ListUserRoleQuery(principal.tenantId(), userId);
        return ResponseEntity.ok(listUserRoleUseCase.execute(query));
    }

    @GetMapping("/admin/tenants/{tenantId}/users/{userId}/roles")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListUserRolesResponse> listUserRolesCrossTenant(
        @PathVariable("tenantId") UUID tenantId,
        @PathVariable("userId") UUID userId
    ) {
        ListUserRoleQuery query = new ListUserRoleQuery(tenantId, userId);
        return ResponseEntity.ok(listUserRoleUseCase.execute(query));
    }
}
