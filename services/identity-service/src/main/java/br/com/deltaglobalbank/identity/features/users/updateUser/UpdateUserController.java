package br.com.deltaglobalbank.identity.features.users.updateUser;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UpdateUserController {

    private final UpdateUserUseCase useCase;

    public UpdateUserController(UpdateUserUseCase useCase) {
        this.useCase = useCase;
    }

    @PreAuthorize("hasRole('identity.admin')")
    @PutMapping("/admin/users/{userId}")
    public ResponseEntity<UpdateUserResponse> updateUserForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID userId,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        UpdateUserResponse response = useCase.execute(new UpdateUserCommand(
            userId,
            request.fullName(),
            request.email(),
            principal.tenantId(),
            principal.roles(),
            principal.subject()
        ));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PutMapping("/admin/tenants/{tenantId}/users/{userId}")
    public ResponseEntity<UpdateUserResponse> updateUserForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID userId,
        @PathVariable UUID tenantId,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        UpdateUserResponse response = useCase.execute(new UpdateUserCommand(
            userId,
            request.fullName(),
            request.email(),
            tenantId,
            principal.roles(),
            principal.subject()
        ));
        return ResponseEntity.ok(response);
    }
}
