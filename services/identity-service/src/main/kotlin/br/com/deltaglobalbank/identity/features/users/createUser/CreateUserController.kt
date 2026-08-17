package br.com.deltaglobalbank.identity.features.users.createUser

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

@Controller()
class CreateUserController(private val createUserUseCase: CreateUserUseCase) {

    @PreAuthorize("hasRole('identity.admin')")
    @PostMapping("/admin/users")
    fun createUserWithImplicitTenantId(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<CreateUserResponse> {
        val command = CreateUserCommand(
            tenantId = principal.tenantId,
            fullName = request.fullName,
            email = request.email.value,
            roleCodes = request.roleCodes,
            grantedBy = principal.subject,
            creatorRoles = principal.roles
        )

        val response = createUserUseCase.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PreAuthorize("hasRole('platform.admin')")
    @PostMapping("/admin/tenants/{tenantId}/users")
    fun createUserWithExplicitTenantId(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @PathVariable tenantId: UUID,
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<CreateUserResponse> {
        val command = CreateUserCommand(
            tenantId = tenantId,
            fullName = request.fullName,
            email = request.email.value,
            roleCodes = request.roleCodes,
            grantedBy = principal.subject,
            creatorRoles = principal.roles
        )
        val response = createUserUseCase.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}