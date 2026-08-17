package br.com.deltaglobalbank.identity.features.tokens.revokeToken

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/admin")
class RevokeTokenController(private val revokeTokenUseCase: RevokeTokenUseCase) {

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/tokens/revoke")
    fun revokeJti(@RequestBody request: RevokeJtiRequest): ResponseEntity<Void> {
        revokeTokenUseCase.revokeJti(request.jti)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasAnyRole('platform.admin', 'identity.admin')")
    @PostMapping("/users/{userId}/tokens/revoke-all")
    fun revokeAllTokens(
        @PathVariable userId: UUID,
        @AuthenticationPrincipal principal: AuthenticatedPrincipal
    ): ResponseEntity<Void> {
        revokeTokenUseCase.revokeAllForUser(userId, principal.tenantId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/tenants/{tenantId}/users/{userId}/tokens/revoke-all")
    fun revokeAllCrossTenant(@PathVariable tenantId: UUID, @PathVariable userId: UUID): ResponseEntity<Void> {
        revokeTokenUseCase.revokeAllForUser(userId, tenantId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}
