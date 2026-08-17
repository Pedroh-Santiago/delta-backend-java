package br.com.deltaglobalbank.identity.features.roles.createRoles;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
    @NotBlank(message = "code é obrigatório")
    String code,

    @NotBlank(message = "label é obrigatório")
    @Size(max = 255, message = "label deve ter no máximo 255 caracteres")
    String label,

    UUID moduleId,

    @Size(max = 255, message = "description deve ter no máximo 255 caracteres")
    String description
) {
}
