package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class CreateTenantIpAllowlistRequest (
    @field:NotBlank
    var cidr: String,
    @field:Size(max = 255)
    var description: String? = null
)