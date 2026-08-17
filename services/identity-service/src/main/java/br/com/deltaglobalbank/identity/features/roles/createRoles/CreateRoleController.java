package br.com.deltaglobalbank.identity.features.roles.createRoles;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CreateRoleController {

    private final CreateRoleUseCase createRoleUseCase;

    public CreateRoleController(CreateRoleUseCase createRoleUseCase) {
        this.createRoleUseCase = createRoleUseCase;
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<CreateRoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        CreateRoleCommand command = new CreateRoleCommand(
            request.code(),
            request.label(),
            request.moduleId(),
            request.description()
        );
        CreateRoleResponse response = createRoleUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
