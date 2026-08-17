package br.com.deltaglobalbank.customers.features.customers.updateCustomer

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Controller
class UpdateCustomerController(private val useCase: UpdateCustomerUseCase) {
    @PreAuthorize("hasAnyRole('customers.operator','customers.admin','customers.write')")
    @PutMapping("/customers/{customerId}")
    fun update(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable customerId: UUID,
        @RequestBody request: UpdateCustomerRequest,
    ): ResponseEntity<UpdateCustomerResponse> {
        val response = useCase.execute(
            UpdateCustomerCommand(customerId, principal.tenantId, principal.subject, request)
        )
        return ResponseEntity.ok(response)
    }
}