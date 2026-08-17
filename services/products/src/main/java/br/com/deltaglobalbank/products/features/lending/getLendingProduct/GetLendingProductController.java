package br.com.deltaglobalbank.products.features.lending.getLendingProduct;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GetLendingProductController {

    private final GetLendingProductUseCase useCase;

    public GetLendingProductController(GetLendingProductUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/products/lending/{productId}")
    @PreAuthorize("hasAnyRole('products.lending.read','products.lending.admin')")
    public ResponseEntity<GetLendingProductResponse> getProductForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID productId
    ) {
        GetLendingProductResponse response = useCase.execute(
            new GetLendingProductCommand(productId, principal.tenantId())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/tenants/{tenantId}/lending/{productId}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<GetLendingProductResponse> getProductForTenant(
        @PathVariable UUID tenantId,
        @PathVariable UUID productId
    ) {
        GetLendingProductResponse response = useCase.execute(
            new GetLendingProductCommand(productId, tenantId)
        );
        return ResponseEntity.ok(response);
    }
}
