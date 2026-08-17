package br.com.deltaglobalbank.identity.features.apiClients.revokeApiKey

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class RevokeApiKeyController(private val revokeApiKeyUseCase: RevokeApiKeyUseCase) {
        @DeleteMapping("/admin/api-keys/{apiKeyId}")
        @PreAuthorize("hasRole('identity.admin')")
        fun revokeApiKeyForOwnTenant(
            @AuthenticationPrincipal principal: AuthenticatedPrincipal,
            @PathVariable("apiKeyId") apiKeyId: UUID
        ): ResponseEntity<Void> {
            val command = RevokeApiKeyCommand(apiKeyId, principal.tenantId, principal.subject)
            revokeApiKeyUseCase.execute(command)
            return ResponseEntity.noContent().build()
        }

    @DeleteMapping("/admin/tenants/{tenantId}/api-keys/{apiKeyId}")
    @PreAuthorize("hasRole('platform.admin')")
    fun revokeApiKeyForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("tenantId") tenantId: UUID,
        @PathVariable("apiKeyId") apiKeyId: UUID
    ): ResponseEntity<Void> {
        val command = RevokeApiKeyCommand(apiKeyId, tenantId, principal.subject)
        revokeApiKeyUseCase.execute(command)
        return ResponseEntity.noContent().build()
    }

}