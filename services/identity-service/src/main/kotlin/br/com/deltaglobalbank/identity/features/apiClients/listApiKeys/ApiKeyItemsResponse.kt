package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys

import java.time.Instant
import java.util.UUID

data class ApiKeyItemsResponse(
    val id: UUID,
    val name: String,
    val keyPrefix: String,
    val expiresAt: Instant?,
    val revokedAt: Instant?,
    val lastUsedAt: Instant?,
    val createdAt: Instant
)
