package br.com.deltaglobalbank.customers.features.customers.createCustomer

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class CreateCustomerController(
    private val createCustomerUseCase: CreateCustomerUseCase,
) {
    @PreAuthorize("hasAnyRole('customers.operator','customers.admin','customers.write')")
    @PostMapping("/customers")
    fun create(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid @RequestBody request: CreateCustomerRequest,
    ): ResponseEntity<CreateCustomerResponse> {
        val command = CreateCustomerCommand(
            tenantId = principal.tenantId,
            createdBy = principal.subject,
            request = request,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(createCustomerUseCase.execute(command))
    }
}