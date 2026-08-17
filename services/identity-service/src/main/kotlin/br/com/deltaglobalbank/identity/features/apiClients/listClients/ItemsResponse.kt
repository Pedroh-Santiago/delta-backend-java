package br.com.deltaglobalbank.identity.features.apiClients.listClients

import java.time.Instant
import java.util.UUID

data class ItemsResponse(
    val id: UUID,
    val tenantId: UUID,
    val tenantSlug: String,
    val name: String,
    val description: String,
    val status: String,
    val roles : List<String>,
    val activeKeysCount: Int,
    val createdAt: Instant)
