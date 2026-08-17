package br.com.deltaglobalbank.identity.features.users.listUserRoles

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class ListUserRolesController(
    private val listUserRoleUseCase: ListUserRoleUseCase
) {

    @GetMapping("/admin/users/{userId}/roles")
    @PreAuthorize("hasRole('identity.admin')")
    fun listUserRolesForOwnTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("userId") userId: UUID
    ): ResponseEntity<ListUserRolesResponse> {
        val query = ListUserRoleQuery(
            tenantId = principal.tenantId,
            userId = userId
        )
        return ResponseEntity.ok(listUserRoleUseCase.execute(query))
    }

    @GetMapping("/admin/tenants/{tenantId}/users/{userId}/roles")
    @PreAuthorize("hasRole('platform.admin')")
    fun listUserRolesCrossTenant(
        @PathVariable("tenantId") tenantId: UUID,
        @PathVariable("userId") userId: UUID
    ): ResponseEntity<ListUserRolesResponse> {
        val query = ListUserRoleQuery(
            tenantId = tenantId,
            userId = userId
        )
        return ResponseEntity.ok(listUserRoleUseCase.execute(query))
    }
}