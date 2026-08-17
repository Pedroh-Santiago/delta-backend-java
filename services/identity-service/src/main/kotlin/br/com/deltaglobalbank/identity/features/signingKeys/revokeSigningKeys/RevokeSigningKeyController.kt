package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import java.util.UUID

@Controller
class RevokeSigningKeyController(
    private val revokeSigningKeyUseCase: RevokeSigningKeyUseCase
) {
    @PostMapping("/admin/signing-keys/{id}/revoke")
    @PreAuthorize("hasRole('platform.admin')")
    fun revokeSigningKey(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        revokeSigningKeyUseCase.execute(id, principal.subject)
        return ResponseEntity.noContent().build()
    }

}