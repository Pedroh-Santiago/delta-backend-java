package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RevokeSigningKeyController {

    private final RevokeSigningKeyUseCase revokeSigningKeyUseCase;

    public RevokeSigningKeyController(RevokeSigningKeyUseCase revokeSigningKeyUseCase) {
        this.revokeSigningKeyUseCase = revokeSigningKeyUseCase;
    }

    @PostMapping("/admin/signing-keys/{id}/revoke")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<Void> revokeSigningKey(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID id
    ) {
        revokeSigningKeyUseCase.execute(id, principal.subject());
        return ResponseEntity.noContent().build();
    }
}
