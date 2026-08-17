package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@Controller
class ListTenantIpAllowlistController(
    private val listTenantIpAllowlistUseCase: ListTenantIpAllowlistUseCase
) {
    @GetMapping("/admin/tenant-ip-allowlist")
    @PreAuthorize("hasRole('identity.admin')")
    fun listForOwnTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal
    ): ResponseEntity<ListTenantIpAllowlistResponse> {
        val query = ListTenantIpAllowlistQuery(
            tenantId = principal.tenantId
        )
        return ResponseEntity.ok(listTenantIpAllowlistUseCase.execute(query))
    }

    @GetMapping("/admin/tenants/{tenantId}/ip-allowlist")
    @PreAuthorize("hasRole('platform.admin')")
    fun listForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable("tenantId") tenantId: UUID,
    ): ResponseEntity<ListTenantIpAllowlistResponse> {
        val query = ListTenantIpAllowlistQuery(
            tenantId = tenantId
        )
        return ResponseEntity.ok(listTenantIpAllowlistUseCase.execute(query))
    }
}