package br.com.deltaglobalbank.identity.features.tenants.createTenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
    @NotBlank(message = "name é obrigatório")
    @Size(min = 3, max = 255, message = "name deve ter entre 3 e 255 caracteres")
    String name,

    @NotBlank(message = "slug é obrigatório")
    String slug
) {
}
