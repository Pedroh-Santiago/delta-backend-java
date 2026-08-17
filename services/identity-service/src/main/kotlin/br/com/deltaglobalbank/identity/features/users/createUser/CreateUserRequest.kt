package br.com.deltaglobalbank.identity.features.users.createUser

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import jakarta.validation.constraints.NotBlank
import br.com.deltaglobalbank.identity.domain.user.Email

data class CreateUserRequest(
    @field:NotBlank
    var email: Email,

    @field:NotBlank
    var fullName: String,

    val roleCodes: List<RoleCode> = emptyList()
)