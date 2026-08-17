package br.com.deltaglobalbank.identity.features.users.resetPassword;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ResetPasswordController {

    private final ResetPasswordUseCase useCase;

    public ResetPasswordController(ResetPasswordUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/admin/users/{userId}/reset-password")
    @PreAuthorize("hasRole('identity.admin')")
    public ResponseEntity<ResetPasswordResponse> resetPasswordForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID userId
    ) {
        ResetPasswordResponse response = useCase.execute(userId, principal.tenantId(), principal.subject());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/reset-password")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ResetPasswordResponse> resetPasswordForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID userId,
        @PathVariable UUID tenantId
    ) {
        ResetPasswordResponse response = useCase.execute(userId, tenantId, principal.subject());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
