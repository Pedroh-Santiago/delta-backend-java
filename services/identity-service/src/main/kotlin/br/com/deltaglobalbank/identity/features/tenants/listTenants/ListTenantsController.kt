package br.com.deltaglobalbank.identity.features.tenants.listTenants

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ListTenantsController(
    private val listTenantsUseCase: ListTenantsUseCase
) {

    @GetMapping("/admin/tenants")
    @PreAuthorize("hasRole('platform.admin')")
    fun listTenants(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ListTenantsResponse> {
        val query = ListTenantsQuery(page = page, size = size)
        return ResponseEntity.ok(listTenantsUseCase.execute(query))
    }
}
