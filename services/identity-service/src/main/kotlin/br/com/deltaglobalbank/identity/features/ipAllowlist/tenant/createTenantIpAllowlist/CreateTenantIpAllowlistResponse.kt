package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import java.time.Instant
import java.util.UUID

data class CreateTenantIpAllowlistResponse (
    val id: UUID,
    val tenantId: UUID,
    val cidr: Cidr,
    val description: String?,
    val createdAt: Instant
)