package br.com.deltaglobalbank.identity.features.users.suspendUser;

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
public class SuspendUserController {

    private final SuspendUserUseCase suspendUserUseCase;

    public SuspendUserController(SuspendUserUseCase suspendUserUseCase) {
        this.suspendUserUseCase = suspendUserUseCase;
    }

    @PreAuthorize("hasAnyRole('platform.admin', 'identity.admin')")
    @PostMapping("/admin/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        suspendUserUseCase.suspendUser(userId, principal.tenantId(), principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUserByTenant(
        @PathVariable UUID tenantId,
        @PathVariable UUID userId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        suspendUserUseCase.suspendUser(userId, tenantId, principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
