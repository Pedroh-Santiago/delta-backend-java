package br.com.deltaglobalbank.customers.features.customers.deleteCustomer

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class DeleteCustomerController(private val useCase: DeleteCustomerUseCase) {
    @PreAuthorize("hasRole('customers.admin')")
    @DeleteMapping("/customers/{customerId}")
    fun delete(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable customerId: UUID,
    ): ResponseEntity<Void> {
        useCase.execute(DeleteCustomerCommand(customerId, principal.tenantId, principal.subject))
        return ResponseEntity.noContent().build()
    }
}