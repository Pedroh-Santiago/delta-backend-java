package br.com.deltaglobalbank.identity.features.users.createUser;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CreateUserController {

    private final CreateUserUseCase createUserUseCase;

    public CreateUserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PreAuthorize("hasRole('identity.admin')")
    @PostMapping("/admin/users")
    public ResponseEntity<CreateUserResponse> createUserWithImplicitTenantId(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @Valid @RequestBody CreateUserRequest request
    ) {
        CreateUserCommand command = new CreateUserCommand(
            principal.tenantId(),
            request.fullName(),
            request.email().value(),
            request.roleCodes(),
            principal.subject(),
            principal.roles()
        );

        CreateUserResponse response = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users")
    public ResponseEntity<CreateUserResponse> createUserWithExplicitTenantId(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID tenantId,
        @Valid @RequestBody CreateUserRequest request
    ) {
        CreateUserCommand command = new CreateUserCommand(
            tenantId,
            request.fullName(),
            request.email().value(),
            request.roleCodes(),
            principal.subject(),
            principal.roles()
        );
        CreateUserResponse response = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
