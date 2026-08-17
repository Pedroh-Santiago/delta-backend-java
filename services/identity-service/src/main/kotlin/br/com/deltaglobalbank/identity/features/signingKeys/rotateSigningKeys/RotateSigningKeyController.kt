package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping

@Controller
class RotateSigningKeyController(
    private val rotateSigningKeyUseCase: RotateSigningKeyUseCase
) {
    @PostMapping("/admin/signing-keys/rotate")
    @PreAuthorize("hasRole('platform.admin')")
    fun rotateSigningKey(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal
    ): ResponseEntity<RotateSigningKeyResponse> {
        val response = rotateSigningKeyUseCase.execute(principal.subject)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}