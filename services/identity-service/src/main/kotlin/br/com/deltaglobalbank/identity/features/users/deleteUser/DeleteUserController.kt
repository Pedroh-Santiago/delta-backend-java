package br.com.deltaglobalbank.identity.features.users.deleteUser

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class DeleteUserController(private val deleteUserUseCase: DeleteUserUseCase) {

    @PreAuthorize("hasAnyRole('platform.admin','identity.admin')")
    @DeleteMapping("/admin/users/{userId}")
    fun deleteUser(@PathVariable userId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void> {
        deleteUserUseCase.deleteUser(userId, principal.tenantId, principal.subject)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole('platform.admin')")
    @DeleteMapping("/admin/tenants/{tenantId}/users/{userId}")
    fun deleteUserByTenant(@PathVariable userId: UUID, @PathVariable tenantId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void> {
        deleteUserUseCase.deleteUser(userId, tenantId, principal.subject)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}