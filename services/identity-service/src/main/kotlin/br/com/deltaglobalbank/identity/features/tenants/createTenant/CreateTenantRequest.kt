package br.com.deltaglobalbank.identity.features.tenants.createTenant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTenantRequest(
    @field:NotBlank(message = "name é obrigatório")
    @field:Size(min = 3, max = 255, message = "name deve ter entre 3 e 255 caracteres")
    val name: String,

    @field:NotBlank(message = "slug é obrigatório")
    val slug: String
)