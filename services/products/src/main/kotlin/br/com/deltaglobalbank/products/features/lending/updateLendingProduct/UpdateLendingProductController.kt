package br.com.deltaglobalbank.products.features.lending.updateLendingProduct

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Controller
class UpdateLendingProductController(
    private val updateLendingProductUseCase: UpdateLendingProductUseCase
) {
    @PutMapping("/products/lending/{productId}")
    @PreAuthorize("hasAnyRole('products.lending.update','products.lending.admin')")
    fun updateLendingProductForOwnTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("productId") productId: UUID,
        @Valid @RequestBody request: UpdateLendingProductRequest
    ): ResponseEntity<UpdateLendingProductResponse> {
        val command = UpdateLendingProductCommand(
            productId = productId,
            tenantId = principal.tenantId,
            updatedBy = principal.subject,
            request = request
        )
        return ResponseEntity.status(HttpStatus.OK).body(updateLendingProductUseCase.execute(command))
    }

    @PutMapping("/products/tenants/{tenantId}/lending/{productId}")
    @PreAuthorize("hasRole('platform.admin')")
    fun updateLendingProductForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("productId") productId: UUID,
        @PathVariable("tenantId") tenantId: UUID,
        @Valid @RequestBody request: UpdateLendingProductRequest
    ): ResponseEntity<UpdateLendingProductResponse> {
        val command = UpdateLendingProductCommand(
            productId = productId,
            tenantId = tenantId,
            updatedBy = principal.subject,
            request = request
        )
        return ResponseEntity.status(HttpStatus.OK).body(updateLendingProductUseCase.execute(command))
    }
}