package br.com.deltaglobalbank.products.features.lending.listLendingProduct

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Controller
class ListLendingProductsController(private val useCase: ListLendingProductsUseCase) {

    @GetMapping("/products/lending")
    @PreAuthorize("hasAnyRole('products.lending.read','products.lending.admin')")
    fun listForOwnTenant(
        @AuthenticationPrincipal p: AuthenticatedPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) agreementName: String?,
        @RequestParam(required = false) active: Boolean?,
    ): ResponseEntity<ListLendingProductsResponse> =
        ResponseEntity.ok(useCase.execute(ListLendingProductsQuery(p.tenantId, page, size, agreementName, active)))

    @GetMapping("/products/tenants/{tenantId}/lending")
    @PreAuthorize("hasRole('platform.admin')")
    fun listForTenant(
        @PathVariable tenantId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) agreementName: String?,
        @RequestParam(required = false) active: Boolean?,
    ): ResponseEntity<ListLendingProductsResponse> =
        ResponseEntity.ok(useCase.execute(ListLendingProductsQuery(tenantId, page, size, agreementName, active)))
}