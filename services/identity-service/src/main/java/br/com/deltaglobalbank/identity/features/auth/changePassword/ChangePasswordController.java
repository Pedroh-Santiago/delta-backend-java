package br.com.deltaglobalbank.identity.features.auth.changePassword;

import br.com.deltaglobalbank.identity.domain.user.CurrentPasswordIncorrectException;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class ChangePasswordController {

    private final ChangePasswordUseCase changePasswordUseCase;

    public ChangePasswordController(ChangePasswordUseCase changePasswordUseCase) {
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (!principal.isUser()) {
            throw new CurrentPasswordIncorrectException();
        }

        changePasswordUseCase.execute(principal.subject(), request);
        return ResponseEntity.ok(new ChangePasswordResponse());
    }
}
