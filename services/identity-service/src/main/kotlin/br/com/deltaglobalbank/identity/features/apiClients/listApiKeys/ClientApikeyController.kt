package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
class ClientApikeyController(private val listApiKeyClientUseCase: ListApiKeyClientUseCase) {

    @PreAuthorize("hasRole('identity.admin')")
    @GetMapping("/admin/api-clients/{apiClientId}/keys")
    fun listClientApiKey(@AuthenticationPrincipal principal: AuthenticatedPrincipal, @PathVariable apiClientId: UUID, @RequestParam(value = "page", defaultValue = "0")page: Int, @RequestParam(value = "size", defaultValue = "20") size: Int) : ResponseEntity<ClientApiKeyResponse> {
        val response = listApiKeyClientUseCase.getClientApiKey(apiClientId, principal.tenantId, page, size)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }


    @PreAuthorize("hasRole('platform.admin')")
    @GetMapping("/admin/tenants/{tenantId}/api-clients/{apiClientId}/keys")
    fun listClientApiKeyByTenantId(@PathVariable apiClientId: UUID, @PathVariable tenantId: UUID, @RequestParam(value = "page", defaultValue = "0")page: Int, @RequestParam(value = "size", defaultValue = "20") size: Int) : ResponseEntity<ClientApiKeyResponse> {
        val response = listApiKeyClientUseCase.getClientApiKey(apiClientId,tenantId , page, size)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }
}