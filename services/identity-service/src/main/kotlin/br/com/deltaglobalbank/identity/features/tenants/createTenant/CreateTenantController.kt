package br.com.deltaglobalbank.identity.features.tenants.createTenant

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CreateTenantController(
    private val createTenantUseCase: CreateTenantUseCase
) {

    @PostMapping("/admin/tenants")
    @PreAuthorize("hasRole('platform.admin')")
    fun create(
        @Valid @RequestBody request: CreateTenantRequest
    ): ResponseEntity<CreateTenantResponse> {
        val command = CreateTenantCommand(
            name = request.name,
            slug = request.slug
        )
        val response = createTenantUseCase.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}