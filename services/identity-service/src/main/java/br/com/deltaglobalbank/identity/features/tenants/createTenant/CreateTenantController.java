package br.com.deltaglobalbank.identity.features.tenants.createTenant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateTenantController {

    private final CreateTenantUseCase createTenantUseCase;

    public CreateTenantController(CreateTenantUseCase createTenantUseCase) {
        this.createTenantUseCase = createTenantUseCase;
    }

    @PostMapping("/admin/tenants")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<CreateTenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        CreateTenantCommand command = new CreateTenantCommand(request.name(), request.slug());
        CreateTenantResponse response = createTenantUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
