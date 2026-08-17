package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ListCustomerController {

    private final ListCustomerUseCase listCustomerUseCase;

    public ListCustomerController(ListCustomerUseCase listCustomerUseCase) {
        this.listCustomerUseCase = listCustomerUseCase;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('customers.viewer','customers.operator','customers.admin','customers.read')")
    public ResponseEntity<ListCustomerResponse> listCustomers(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) Integer size
    ) {
        ListCustomerQuery query = new ListCustomerQuery(principal.tenantId(), page, size);
        return ResponseEntity.ok(listCustomerUseCase.execute(query));
    }
}
