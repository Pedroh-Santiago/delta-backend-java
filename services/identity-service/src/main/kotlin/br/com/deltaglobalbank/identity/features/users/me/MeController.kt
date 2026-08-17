package br.com.deltaglobalbank.identity.features.users.me

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MeController(
    private val meUseCase: MeUseCase
) {

    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal
    ): ResponseEntity<MeResponse> {
        val query = MeQuery(
            principalId = principal.subject,
            principalType = principal.principalType
        )
        return ResponseEntity.ok(meUseCase.execute(query))
    }
}