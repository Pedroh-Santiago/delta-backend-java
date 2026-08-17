package br.com.deltaglobalbank.identity.features.users.suspendUser

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
class SuspendUserController(private val suspendUserUseCase: SuspendUserUseCase) {

    @PreAuthorize("hasAnyRole('platform.admin', 'identity.admin')")
    @PostMapping("/admin/users/{userId}/suspend")
    fun suspendUser(@PathVariable userId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void> {
        suspendUserUseCase.suspendUser(userId, principal.tenantId, principal.subject)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/suspend")
    fun suspendUserByTenant(@PathVariable tenantId: UUID, @PathVariable userId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void> {
        suspendUserUseCase.suspendUser(userId, tenantId, principal.subject)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }


}