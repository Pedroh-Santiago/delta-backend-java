package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class DeleteLendingProductController(
    private val useCase: DeleteLendingProductUseCase
) {
    @DeleteMapping("/products/lending/{productId}")
    @PreAuthorize("hasAnyRole('products.lending.delete','products.lending.admin')")
    fun deleteForOwnTenant(
            @AuthenticationPrincipal principal: AuthenticatedPrincipal,
            @PathVariable productId: UUID,
        ): ResponseEntity<Void> {
        useCase.execute(DeleteLendingProductCommand(productId, principal.tenantId, principal.subject))
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/products/tenants/{tenantId}/lending/{productId}")
    @PreAuthorize("hasRole('platform.admin')")
    fun deleteByTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable tenantId: UUID,
        @PathVariable productId: UUID
    ): ResponseEntity<Void> {
        useCase.execute(DeleteLendingProductCommand(productId, tenantId, principal.subject))
        return ResponseEntity.noContent().build()
    }
}
