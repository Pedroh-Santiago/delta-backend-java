package br.com.deltaglobalbank.identity.features.users.deleteUser;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteUserController {

    private final DeleteUserUseCase deleteUserUseCase;

    public DeleteUserController(DeleteUserUseCase deleteUserUseCase) {
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @PreAuthorize("hasAnyRole('platform.admin','identity.admin')")
    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        deleteUserUseCase.deleteUser(userId, principal.tenantId(), principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @DeleteMapping("/admin/tenants/{tenantId}/users/{userId}")
    public ResponseEntity<Void> deleteUserByTenant(
        @PathVariable UUID userId,
        @PathVariable UUID tenantId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        deleteUserUseCase.deleteUser(userId, tenantId, principal.subject());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
