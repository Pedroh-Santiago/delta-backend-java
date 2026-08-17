package br.com.deltaglobalbank.identity.features.users.activateUser;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivateUserController {

    private final ActivateUserUseCase activeUserUseCase;

    public ActivateUserController(ActivateUserUseCase activeUserUseCase) {
        this.activeUserUseCase = activeUserUseCase;
    }

    @PreAuthorize("hasAnyRole('platform.admin', 'identity.admin')")
    @PostMapping("/admin/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        activeUserUseCase.activateUser(userId, principal.tenantId(), principal.subject());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/activate")
    public ResponseEntity<Void> activateUserByTenant(
        @PathVariable UUID tenantId,
        @PathVariable UUID userId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        activeUserUseCase.activateUser(userId, tenantId, principal.subject());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
