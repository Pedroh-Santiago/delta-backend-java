package br.com.deltaglobalbank.identity.features.apiClients.createApiClient

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class CreateApiClientRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    var name: String,
    @field:Size(max = 2000)
    var description: String? = null,
    var roleCodes: List<RoleCode> = emptyList()
)