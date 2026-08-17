package br.com.deltaglobalbank.identity.features.tenants.createTenant

import java.time.Instant
import java.util.UUID

data class CreateTenantResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val status: String,
    val enabledModules: List<String>,
    val createdAt: Instant
)