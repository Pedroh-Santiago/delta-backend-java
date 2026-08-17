package br.com.deltaglobalbank.identity.features.apiClients.createApiKey

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class CreateApiKeyController(
    private val createApiKeyUseCase: CreateApiKeyUseCase
) {

    @PostMapping("/admin/api-clients/{apiClientId}/keys")
    @PreAuthorize("hasRole('identity.admin')")
    fun createInOwnTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable apiClientId: UUID,
        @Valid @RequestBody request: CreateApiKeyRequest
    ): ResponseEntity<CreateApiKeyResponse> {
        val command = CreateApiKeyCommand(
            apiClientId = apiClientId,
            expectedTenantId = principal.tenantId,  // força ownership
            name = request.name,
            expiresAt = request.expiresAt
        )
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createApiKeyUseCase.execute(command))
    }

    @PostMapping("/admin/tenants/{tenantId}/api-clients/{apiClientId}/keys")
    @PreAuthorize("hasRole('platform.admin')")
    fun createInSpecificTenant(
        @PathVariable tenantId: UUID,
        @PathVariable apiClientId: UUID,
        @Valid @RequestBody request: CreateApiKeyRequest
    ): ResponseEntity<CreateApiKeyResponse> {
        val command = CreateApiKeyCommand(
            apiClientId = apiClientId,
            expectedTenantId = tenantId,
            name = request.name,
            expiresAt = request.expiresAt
        )
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createApiKeyUseCase.execute(command))
    }
}