package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

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
public class UpdateCustomerController {

    private final UpdateCustomerUseCase useCase;

    public UpdateCustomerController(UpdateCustomerUseCase useCase) {
        this.useCase = useCase;
    }

    @PreAuthorize("hasAnyRole('customers.operator','customers.admin','customers.write')")
    @PutMapping("/customers/{customerId}")
    public ResponseEntity<UpdateCustomerResponse> update(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID customerId,
        @Valid @RequestBody UpdateCustomerRequest request
    ) {
        UpdateCustomerResponse response = useCase.execute(
            new UpdateCustomerCommand(customerId, principal.tenantId(), principal.subject(), request)
        );
        return ResponseEntity.ok(response);
    }
}
