package br.com.deltaglobalbank.identity.features.users.updateUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Size(max = 255) @Email String email
) {
}
