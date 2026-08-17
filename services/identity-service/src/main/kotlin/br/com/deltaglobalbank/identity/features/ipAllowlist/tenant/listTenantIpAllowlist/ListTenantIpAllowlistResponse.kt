package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import java.time.Instant
import java.util.UUID

data class ListTenantIpAllowlistResponse(
    val items: List<ListedTenantIpAllowlist>
)

data class ListedTenantIpAllowlist (
    val id: UUID,
    val tenantId: UUID,
    val cidr: Cidr,
    val description: String?,
    val createdAt: Instant
)