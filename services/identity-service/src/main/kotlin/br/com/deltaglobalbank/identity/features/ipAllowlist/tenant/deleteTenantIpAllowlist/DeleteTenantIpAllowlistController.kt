package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class DeleteTenantIpAllowlistController(
    private val deleteTenantIpAllowlistUseCase: DeleteTenantIpAllowlistUseCase
) {
    @DeleteMapping("/admin/tenants/{tenantId}/ip-allowlist/{id}")
    @PreAuthorize("hasRole('platform.admin')")
    fun deleteForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("tenantId") tenantId: UUID,
        @PathVariable("id") id: UUID
    ): ResponseEntity<Void> {
        val command = DeleteTenantIpAllowlistCommand(id = id, tenantId = tenantId)
        deleteTenantIpAllowlistUseCase.execute(command)
        return ResponseEntity.noContent().build()
    }
}