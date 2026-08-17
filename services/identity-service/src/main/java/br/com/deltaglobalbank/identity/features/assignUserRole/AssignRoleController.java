package br.com.deltaglobalbank.identity.features.assignUserRole;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
public class AssignRoleController {

    private final AssignRoleUseCase assignRoleUseCase;

    public AssignRoleController(AssignRoleUseCase assignRoleUseCase) {
        this.assignRoleUseCase = assignRoleUseCase;
    }

    @PreAuthorize("hasRole('identity.admin')")
    @PostMapping("/admin/users/{userId}/roles")
    public ResponseEntity<AssignRolesResponse> assignUserRoleTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID userId,
        @Valid @RequestBody AssignRolesRequest assignRolesRequest
    ) {
        AssignRolesResponse response = assignRoleUseCase.assignRoleTenant(
            principal.tenantId(), principal, userId, assignRolesRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/roles")
    public ResponseEntity<AssignRolesResponse> assignUserRoleCrossTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID tenantId,
        @PathVariable UUID userId,
        @Valid @RequestBody AssignRolesRequest assignRolesRequest
    ) {
        AssignRolesResponse response = assignRoleUseCase.assignRoleTenant(
            tenantId, principal, userId, assignRolesRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
