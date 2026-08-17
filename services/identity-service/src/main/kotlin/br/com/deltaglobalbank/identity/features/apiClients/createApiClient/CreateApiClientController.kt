package br.com.deltaglobalbank.identity.features.apiClients.createApiClient

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Controller
class CreateApiClientController(private val createApiClientUseCase: CreateApiClientUseCase) {

    @PreAuthorize("hasRole('identity.admin')")
    @PostMapping("/admin/api-clients")
    fun createForOwnTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid
        @RequestBody request: CreateApiClientRequest
    ): ResponseEntity<CreateApiClientResponse> {
        val command = CreateApiClientCommand(
            tenantId = principal.tenantId,
            name = request.name,
            description = request.description,
            roleCodes = request.roleCodes,
            creatorRoles = principal.roles
        )

        val response = createApiClientUseCase.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/api-clients")
    fun createForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable tenantId: UUID,
        @Valid
        @RequestBody request: CreateApiClientRequest
    ): ResponseEntity<CreateApiClientResponse> {
        val command = CreateApiClientCommand(
            tenantId = tenantId,
            name = request.name,
            description = request.description,
            roleCodes = request.roleCodes,
            creatorRoles = principal.roles
        )
        val response = createApiClientUseCase.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

}