package br.com.deltaglobalbank.identity.features.tenants.deleteTenant

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
class DeleteTenantController(private val deleteTenantUseCase: DeleteTenantUseCase) {

    @PreAuthorize("hasRole('platform.admin')")
    @DeleteMapping("/admin/tenants/{tenantId}")
    fun deleteTenant(@PathVariable tenantId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void>{
        deleteTenantUseCase.deleteTenant(tenantId, principal.subject)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}