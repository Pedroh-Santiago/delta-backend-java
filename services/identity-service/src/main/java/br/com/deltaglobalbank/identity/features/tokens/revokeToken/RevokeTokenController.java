package br.com.deltaglobalbank.identity.features.tokens.revokeToken;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
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
@RequestMapping("/admin")
public class RevokeTokenController {

    private final RevokeTokenUseCase revokeTokenUseCase;

    public RevokeTokenController(RevokeTokenUseCase revokeTokenUseCase) {
        this.revokeTokenUseCase = revokeTokenUseCase;
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/tokens/revoke")
    public ResponseEntity<Void> revokeJti(@RequestBody RevokeJtiRequest request) {
        revokeTokenUseCase.revokeJti(request.jti());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAnyRole('platform.admin', 'identity.admin')")
    @PostMapping("/users/{userId}/tokens/revoke-all")
    public ResponseEntity<Void> revokeAllTokens(
        @PathVariable UUID userId,
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        revokeTokenUseCase.revokeAllForUser(userId, principal.tenantId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/tenants/{tenantId}/users/{userId}/tokens/revoke-all")
    public ResponseEntity<Void> revokeAllCrossTenant(@PathVariable UUID tenantId, @PathVariable UUID userId) {
        revokeTokenUseCase.revokeAllForUser(userId, tenantId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
