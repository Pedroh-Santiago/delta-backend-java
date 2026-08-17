package br.com.deltaglobalbank.identity.features.assignUserRole

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
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
@RequestMapping()
class AssignRoleController(private val assignRoleUseCase: AssignRoleUseCase) {

    @PreAuthorize("hasRole('identity.admin')")
    @PostMapping("/admin/users/{userId}/roles")
    fun assignUserRoleTenant(@AuthenticationPrincipal principal: AuthenticatedPrincipal, @PathVariable userId: UUID, @Valid @RequestBody assignRolesRequest: AssignRolesRequest ): ResponseEntity<AssignRolesResponse> {
        val response = assignRoleUseCase.assignRoleTenant( principal.tenantId,principal, userId, assignRolesRequest)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users/{userId}/roles")
    fun assignUserRoleCrossTenant(@AuthenticationPrincipal principal: AuthenticatedPrincipal, @PathVariable tenantId: UUID, @PathVariable userId: UUID, @Valid @RequestBody assignRolesRequest: AssignRolesRequest): ResponseEntity<AssignRolesResponse>{
        val response = assignRoleUseCase.assignRoleTenant( tenantId, principal, userId, assignRolesRequest)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

}