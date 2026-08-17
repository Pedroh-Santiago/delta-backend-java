package br.com.deltaglobalbank.products.features.lending.getLendingProduct

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class GetLendingProductController(
    private val useCase: GetLendingProductUseCase
){
        @GetMapping("/products/lending/{productId}")
        @PreAuthorize("hasAnyRole('products.lending.read','products.lending.admin')")
        fun getProductForOwnTenant(
            @AuthenticationPrincipal principal: AuthenticatedPrincipal,
            @PathVariable productId: UUID
        ) : ResponseEntity<GetLendingProductResponse>{
            val response = useCase.execute(
                GetLendingProductCommand(
                    productId,
                    principal.tenantId
                )
            )
            return ResponseEntity.ok(response)
        }

        @GetMapping("/products/tenants/{tenantId}/lending/{productId}")
        @PreAuthorize("hasRole('platform.admin')")
        fun getProductForTenant(
            @PathVariable tenantId: UUID,
            @PathVariable productId: UUID
        ) : ResponseEntity<GetLendingProductResponse>{
         val response = useCase.execute(
             GetLendingProductCommand(
                 productId,
                 tenantId
             )
         )
             return ResponseEntity.ok(response)
        }
}