package br.com.deltaglobalbank.identity.features.users.removeUserRole;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RemoveUserRoleController {

    private final RemoveUserRoleUseCase removeUserRoleUseCase;

    public RemoveUserRoleController(RemoveUserRoleUseCase removeUserRoleUseCase) {
        this.removeUserRoleUseCase = removeUserRoleUseCase;
    }

    @DeleteMapping("/admin/users/{userId}/roles/{roleCode}")
    @PreAuthorize("hasRole('identity.admin')")
    public ResponseEntity<Void> removeUserRoleForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("userId") UUID userId,
        @PathVariable("roleCode") String roleCode
    ) {
        removeUserRoleUseCase.execute(
            new RemoveUserRoleCommand(principal.tenantId(), userId, new RoleCode(roleCode), principal));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/tenants/{tenantId}/users/{userId}/roles/{roleCode}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<Void> removeUserRoleForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("tenantId") UUID tenantId,
        @PathVariable("userId") UUID userId,
        @PathVariable("roleCode") String roleCode
    ) {
        removeUserRoleUseCase.execute(
            new RemoveUserRoleCommand(tenantId, userId, new RoleCode(roleCode), principal));
        return ResponseEntity.noContent().build();
    }
}
