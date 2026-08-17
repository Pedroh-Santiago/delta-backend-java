package br.com.deltaglobalbank.identity.features.auth.login

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "email é obrigatório")
    @field:Email(message = "email inválido")
    val email: String,

    @field:NotBlank(message = "password é obrigatório")
    val password: String
)