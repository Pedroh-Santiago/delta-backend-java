package br.com.deltaglobalbank.products.features.lending.createLendingProduct

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
class CreateLendingProductController(private val useCase: CreateLendingProductUseCase) {
    @PreAuthorize("hasAnyRole('products.lending.create','products.lending.admin')")
    @PostMapping("/products/lending")
    fun create(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid @RequestBody request: CreateLendingProductRequest,
    ): ResponseEntity<CreateLendingProductResponse> {
        val command = CreateLendingProductCommand(
            tenantId = principal.tenantId,
            createdBy = principal.subject,
            request = request,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.execute(command))
    }
}