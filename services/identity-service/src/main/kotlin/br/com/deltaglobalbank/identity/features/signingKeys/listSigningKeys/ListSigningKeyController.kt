package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ListSigningKeyController(
    private val listSigningKeysUseCase: ListSigningKeysUseCase
) {
    @GetMapping("/admin/signing-keys")
    @PreAuthorize("hasRole('platform.admin')")
    fun listSigningKey(): ResponseEntity<ListSigningKeysResponse> =
        ResponseEntity.ok(listSigningKeysUseCase.execute())
}