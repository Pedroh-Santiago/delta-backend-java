package br.com.deltaglobalbank.customers.features.customers.listCustomer

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ListCustomerController (
    private val listCustomerUseCase: ListCustomerUseCase
){
    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('customers.viewer','customers.operator','customers.admin','customers.read')")    fun listCustomers(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<ListCustomerResponse> {
        val query = ListCustomerQuery(
            tenantId = principal.tenantId,
            page = page,
            size = size
        )

        return ResponseEntity.ok(listCustomerUseCase.execute(query))
    }
}
