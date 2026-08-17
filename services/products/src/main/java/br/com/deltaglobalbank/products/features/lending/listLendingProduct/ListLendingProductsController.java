package br.com.deltaglobalbank.products.features.lending.listLendingProduct;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ListLendingProductsController {

    private final ListLendingProductsUseCase useCase;

    public ListLendingProductsController(ListLendingProductsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/products/lending")
    @PreAuthorize("hasAnyRole('products.lending.read','products.lending.admin')")
    public ResponseEntity<ListLendingProductsResponse> listForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal p,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String agreementName,
        @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(
            useCase.execute(new ListLendingProductsQuery(p.tenantId(), page, size, agreementName, active))
        );
    }

    @GetMapping("/products/tenants/{tenantId}/lending")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListLendingProductsResponse> listForTenant(
        @PathVariable UUID tenantId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String agreementName,
        @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(
            useCase.execute(new ListLendingProductsQuery(tenantId, page, size, agreementName, active))
        );
    }
}
