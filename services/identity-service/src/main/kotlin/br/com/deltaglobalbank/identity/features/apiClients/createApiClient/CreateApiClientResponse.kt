package br.com.deltaglobalbank.identity.features.apiClients.createApiClient

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import java.time.Instant
import java.util.UUID

data class CreateApiClientResponse(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val description: String?,
    val status: String,
    val roles: List<RoleCode>,
    val createdAt: Instant
)
