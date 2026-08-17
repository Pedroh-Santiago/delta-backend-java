package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RotateSigningKeyController {

    private final RotateSigningKeyUseCase rotateSigningKeyUseCase;

    public RotateSigningKeyController(RotateSigningKeyUseCase rotateSigningKeyUseCase) {
        this.rotateSigningKeyUseCase = rotateSigningKeyUseCase;
    }

    @PostMapping("/admin/signing-keys/rotate")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<RotateSigningKeyResponse> rotateSigningKey(
        @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        RotateSigningKeyResponse response = rotateSigningKeyUseCase.execute(principal.subject());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
