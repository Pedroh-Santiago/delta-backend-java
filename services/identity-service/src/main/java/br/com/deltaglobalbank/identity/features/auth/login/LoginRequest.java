package br.com.deltaglobalbank.identity.features.auth.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    String email,

    @NotBlank(message = "password é obrigatório")
    String password
) {
}
