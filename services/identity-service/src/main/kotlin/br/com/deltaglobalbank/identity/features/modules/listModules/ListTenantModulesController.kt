package br.com.deltaglobalbank.identity.features.modules.listTenantModules

import br.com.deltaglobalbank.identity.features.modules.listModules.ListTenantModulesResponse
import br.com.deltaglobalbank.identity.features.modules.listModules.ListTenantModulesUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ListTenantModulesController(
    private val useCase: ListTenantModulesUseCase
) {

    @GetMapping("/admin/tenants/{tenantId}/modules")
    @PreAuthorize("hasRole('platform.admin')")
    fun list(
        @PathVariable tenantId: UUID
    ): ResponseEntity<ListTenantModulesResponse> {
        return ResponseEntity.ok(useCase.execute(tenantId))
    }
}