package br.com.deltaglobalbank.identity.features.roles.listRoles

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ListRolesController(
    private val listRolesUseCase: ListRolesUseCase
) {

    @GetMapping("/admin/roles")
    @PreAuthorize("hasRole('platform.admin') or hasRole('identity.admin')")
    fun listRoles(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal
    ): ResponseEntity<ListRolesResponse> {
        val query = ListRolesQuery(
            includePlatformAdmin = principal.roles.contains("platform.admin")
        )
        return ResponseEntity.ok(listRolesUseCase.execute(query))
    }
}
