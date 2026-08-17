package br.com.deltaglobalbank.customers.features.customers.getCustomer

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class GetCustomerController(
    private val getCustomerUseCase: GetCustomerUseCase
){
    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('customers.viewer','customers.operator','customers.admin','customers.read')")
    fun getCustomer(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("customerId") customerId: UUID
    ): ResponseEntity<GetCustomerResponse> {
        val response = getCustomerUseCase.execute(
            GetCustomerRequest(
                customerId,
                principal.tenantId
            )
        )
        return ResponseEntity.ok(response)
    }
}