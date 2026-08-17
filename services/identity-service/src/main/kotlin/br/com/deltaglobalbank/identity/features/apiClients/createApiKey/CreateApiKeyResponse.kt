package br.com.deltaglobalbank.identity.features.apiClients.createApiKey

import java.time.Instant
import java.util.UUID

data class CreateApiKeyResponse(
    val apiKey: CreatedApiKey,
    val key: String
)

data class CreatedApiKey(
    val id: UUID,
    val apiClientId: UUID,
    val name: String,
    val keyPrefix: String,
    val expiresAt: Instant?,
    val createdAt: Instant
)