package br.com.deltaglobalbank.customers.features.customers.deleteCustomer;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DeleteCustomerController {

    private final DeleteCustomerUseCase useCase;

    public DeleteCustomerController(DeleteCustomerUseCase useCase) {
        this.useCase = useCase;
    }

    @PreAuthorize("hasRole('customers.admin')")
    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID customerId
    ) {
        useCase.execute(new DeleteCustomerCommand(customerId, principal.tenantId(), principal.subject()));
        return ResponseEntity.noContent().build();
    }
}
