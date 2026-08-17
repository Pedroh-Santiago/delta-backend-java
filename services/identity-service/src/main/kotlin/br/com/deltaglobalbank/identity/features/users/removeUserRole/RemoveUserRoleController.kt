package br.com.deltaglobalbank.identity.features.users.removeUserRole

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class RemoveUserRoleController(
    private val removeUserRoleUseCase: RemoveUserRoleUseCase
) {
    @DeleteMapping("/admin/users/{userId}/roles/{roleCode}")
    @PreAuthorize("hasRole('identity.admin')")
    fun removeUserRoleForOwnTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("userId") userId: UUID,
        @PathVariable("roleCode") roleCode: String
    ): ResponseEntity<Void> {
        removeUserRoleUseCase.execute(RemoveUserRoleCommand(principal.tenantId, userId, RoleCode(roleCode), principal))
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/admin/tenants/{tenantId}/users/{userId}/roles/{roleCode}")
    @PreAuthorize("hasRole('platform.admin')")
    fun removeUserRoleForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("tenantId") tenantId: UUID,
        @PathVariable("userId") userId: UUID,
        @PathVariable("roleCode") roleCode: String
    ): ResponseEntity<Void> {
        removeUserRoleUseCase.execute(RemoveUserRoleCommand(tenantId, userId, RoleCode(roleCode), principal))
        return ResponseEntity.noContent().build()
    }
}