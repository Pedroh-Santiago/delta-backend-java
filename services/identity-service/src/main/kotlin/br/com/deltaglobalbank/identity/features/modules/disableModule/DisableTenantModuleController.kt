package br.com.deltaglobalbank.identity.features.modules.disableModule

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class DisableTenantModuleController(
    private val useCase: DisableTenantModuleUseCase
) {

    @DeleteMapping("/admin/tenants/{tenantId}/modules/{moduleCode}")
    @PreAuthorize("hasRole('platform.admin')")
    fun disable(
        @PathVariable tenantId: UUID,
        @PathVariable moduleCode: String
    ): ResponseEntity<Void> {
        useCase.execute(
            DisableTenantModuleCommand(tenantId = tenantId, moduleCode = moduleCode)
        )
        return ResponseEntity.noContent().build()
    }
}