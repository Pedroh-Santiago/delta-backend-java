package br.com.deltaglobalbank.identity.features.users.listUsers

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ListUsersController(
    private val listUsersUseCase: ListUsersUseCase
) {

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('platform.admin') or hasRole('identity.admin')")
    fun listUsers(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ListUsersResponse> {
        val isPlatformAdmin = principal.roles.contains("platform.admin")

        val query = ListUsersQuery(
            tenantIdFilter = if (isPlatformAdmin) null else principal.tenantId,
            page = page,
            size = size,
            requireTenantExists = false
        )

        return ResponseEntity.ok(listUsersUseCase.execute(query))
    }

    @GetMapping("/admin/tenants/{tenantId}/users")
    @PreAuthorize("hasRole('platform.admin')")
    fun listUsersByTenant(
        @PathVariable tenantId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ListUsersResponse> {
        val query = ListUsersQuery(
            tenantIdFilter = tenantId,
            page = page,
            size = size,
            requireTenantExists = true
        )

        return ResponseEntity.ok(listUsersUseCase.execute(query))
    }
}