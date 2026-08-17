package br.com.deltaglobalbank.customers.features.customers.changeCustomerStatus;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ChangeCustomerStatusController {

    private final ChangeCustomerStatusUseCase useCase;

    public ChangeCustomerStatusController(ChangeCustomerStatusUseCase useCase) {
        this.useCase = useCase;
    }

    @PreAuthorize("hasAnyRole('customers.operator','customers.admin','customers.write')")
    @PatchMapping("/customers/{customerId}/status")
    public ResponseEntity<ChangeCustomerStatusResponse> changeStatus(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID customerId,
        @RequestBody ChangeCustomerStatusRequest request
    ) {
        ChangeCustomerStatusResponse response = useCase.execute(
            new ChangeCustomerStatusCommand(customerId, principal.tenantId(), principal.subject(), request)
        );
        return ResponseEntity.ok(response);
    }
}
