package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Controller
class CreateTenantIpAllowlistController(private val createTenantIpAllowlistUsecase: CreateTenantIpAllowlistUsecase) {
    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/ip-allowlist")
    fun createForTenant(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable tenantId: UUID,
        @Valid
        @RequestBody request: CreateTenantIpAllowlistRequest
    ): ResponseEntity<CreateTenantIpAllowlistResponse> {
        val command = CreateTenantIpAllowlistCommand(
            tenantId = tenantId,
            cidr = request.cidr,
            description = request.description
        )

        val response = createTenantIpAllowlistUsecase.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}