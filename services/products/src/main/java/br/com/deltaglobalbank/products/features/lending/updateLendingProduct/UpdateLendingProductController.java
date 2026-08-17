package br.com.deltaglobalbank.products.features.lending.updateLendingProduct;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UpdateLendingProductController {

    private final UpdateLendingProductUseCase updateLendingProductUseCase;

    public UpdateLendingProductController(UpdateLendingProductUseCase updateLendingProductUseCase) {
        this.updateLendingProductUseCase = updateLendingProductUseCase;
    }

    @PutMapping("/products/lending/{productId}")
    @PreAuthorize("hasAnyRole('products.lending.update','products.lending.admin')")
    public ResponseEntity<UpdateLendingProductResponse> updateLendingProductForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("productId") UUID productId,
        @Valid @RequestBody UpdateLendingProductRequest request
    ) {
        UpdateLendingProductCommand command = new UpdateLendingProductCommand(
            productId,
            principal.tenantId(),
            principal.subject(),
            request
        );
        return ResponseEntity.status(HttpStatus.OK).body(updateLendingProductUseCase.execute(command));
    }

    @PutMapping("/products/tenants/{tenantId}/lending/{productId}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<UpdateLendingProductResponse> updateLendingProductForTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable("productId") UUID productId,
        @PathVariable("tenantId") UUID tenantId,
        @Valid @RequestBody UpdateLendingProductRequest request
    ) {
        UpdateLendingProductCommand command = new UpdateLendingProductCommand(
            productId,
            tenantId,
            principal.subject(),
            request
        );
        return ResponseEntity.status(HttpStatus.OK).body(updateLendingProductUseCase.execute(command));
    }
}
