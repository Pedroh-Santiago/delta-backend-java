package br.com.deltaglobalbank.identity.features.modules.enableModule

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class EnableTenantModuleController(
    private val useCase: EnableTenantModuleUseCase
) {

    @PostMapping("/admin/tenants/{tenantId}/modules/{moduleCode}")
    @PreAuthorize("hasRole('platform.admin')")
    fun enable(
        @PathVariable tenantId: UUID,
        @PathVariable moduleCode: String
    ): ResponseEntity<EnableModuleResponse> {
        val response = useCase.execute(
            EnableTenantModuleCommand(tenantId = tenantId, moduleCode = moduleCode)
        )
        return ResponseEntity.ok(response)
    }
}