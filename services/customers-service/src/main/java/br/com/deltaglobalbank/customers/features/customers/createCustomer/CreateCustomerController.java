package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CreateCustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    public CreateCustomerController(CreateCustomerUseCase createCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
    }

    @PreAuthorize("hasAnyRole('customers.operator','customers.admin','customers.write')")
    @PostMapping("/customers")
    public ResponseEntity<CreateCustomerResponse> create(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @Valid @RequestBody CreateCustomerRequest request
    ) {
        CreateCustomerCommand command = new CreateCustomerCommand(
            principal.tenantId(),
            principal.subject(),
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createCustomerUseCase.execute(command));
    }
}
