package br.com.deltaglobalbank.identity.features.auth.changePassword;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank(message = "currentPassword é obrigatório")
    String currentPassword,

    @NotBlank(message = "newPassword é obrigatório")
    String newPassword
) {
}
