package br.com.deltaglobalbank.identity.features.auth.changePassword

import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank(message = "currentPassword é obrigatório")
    val currentPassword: String,

    @field:NotBlank(message = "newPassword é obrigatório")
    val newPassword: String
)