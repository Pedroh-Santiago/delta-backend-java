package br.com.deltaglobalbank.identity.features.tenants.activateTenant

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
class ActivateTenantController(private val activateTenantUseCase: ActivateTenantUseCase) {

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/activate")
    fun activateTenant(@PathVariable tenantId: UUID, @AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<Void>{
        activateTenantUseCase.activateTenant(tenantId, principal.subject)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}