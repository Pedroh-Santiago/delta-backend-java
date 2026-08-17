package br.com.deltaglobalbank.identity.features.auth.changePassword

import br.com.deltaglobalbank.identity.domain.user.CurrentPasswordIncorrectException
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class ChangePasswordController(
    private val changePasswordUseCase: ChangePasswordUseCase
) {

    @PostMapping("/change-password")
    fun changePassword(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ChangePasswordResponse> {
        if (!principal.isUser) {
            // api_client não troca senha
            throw CurrentPasswordIncorrectException()
        }

        changePasswordUseCase.execute(principal.subject, request)
        return ResponseEntity.ok(ChangePasswordResponse())
    }
}