package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GetCustomerController {

    private final GetCustomerUseCase getCustomerUseCase;

    public GetCustomerController(GetCustomerUseCase getCustomerUseCase) {
        this.getCustomerUseCase = getCustomerUseCase;
    }

    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('customers.viewer','customers.operator','customers.admin','customers.read')")
    public ResponseEntity<GetCustomerResponse> getCustomer(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("customerId") UUID customerId
    ) {
        GetCustomerResponse response = getCustomerUseCase.execute(
            new GetCustomerRequest(customerId, principal.tenantId())
        );
        return ResponseEntity.ok(response);
    }
}
