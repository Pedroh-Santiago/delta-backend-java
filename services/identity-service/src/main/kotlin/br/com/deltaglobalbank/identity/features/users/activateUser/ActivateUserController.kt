package br.com.deltaglobalbank.identity.features.users.activateUser

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ActivateUserController(private val activeUserUseCase: ActivateUserUseCase) {

    @PreAuthorize("hasAnyRole('platform.admin', 'identity.admin')")
    @PostMapping("/admin/users/{userId}/activate")
    fun activateUser(@PathVariable userId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void> {
        activeUserUseCase.activateUser(userId, principal.tenantId, principal.subject)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/activate")
    fun activateUserByTenant(@PathVariable tenantId: UUID, @PathVariable userId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void> {
        activeUserUseCase.activateUser(userId, tenantId,principal.subject )
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}