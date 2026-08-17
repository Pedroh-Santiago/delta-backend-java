package br.com.deltaglobalbank.identity.features.tenants.listTenants

import java.time.Instant
import java.util.UUID

data class ListTenantsResponse(
    val items: List<ListedTenant>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class ListedTenant(
    val id: UUID,
    val name: String,
    val slug: String,
    val status: String,
    val createdAt: Instant
)
