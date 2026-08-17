package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct;

import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DeleteLendingProductController {

    private final DeleteLendingProductUseCase useCase;

    public DeleteLendingProductController(DeleteLendingProductUseCase useCase) {
        this.useCase = useCase;
    }

    @DeleteMapping("/products/lending/{productId}")
    @PreAuthorize("hasAnyRole('products.lending.delete','products.lending.admin')")
    public ResponseEntity<Void> deleteForOwnTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID productId
    ) {
        useCase.execute(new DeleteLendingProductCommand(productId, principal.tenantId(), principal.subject()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/products/tenants/{tenantId}/lending/{productId}")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<Void> deleteByTenant(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @PathVariable UUID tenantId,
        @PathVariable UUID productId
    ) {
        useCase.execute(new DeleteLendingProductCommand(productId, tenantId, principal.subject()));
        return ResponseEntity.noContent().build();
    }
}
