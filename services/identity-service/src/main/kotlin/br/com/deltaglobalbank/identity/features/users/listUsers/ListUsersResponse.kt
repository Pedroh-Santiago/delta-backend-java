package br.com.deltaglobalbank.identity.features.users.listUsers

import java.util.UUID
import java.time.Instant

data class ListUsersResponse(
    val items: List<ListedUser>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class ListedUser(
    val id: UUID,
    val fullName: String,
    val email: String,
    val tenantId: UUID,
    val tenantSlug: String,
    val status: String,
    val roles: List<String>,
    val mustChangePassword: Boolean,
    val failedAttempts: Int,
    val lockedUntil: Instant?,
    val lastLoginAt: Instant?,
    val createdAt: Instant
)