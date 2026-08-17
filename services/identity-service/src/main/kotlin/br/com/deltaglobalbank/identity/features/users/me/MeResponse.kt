package br.com.deltaglobalbank.identity.features.users.me

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MeResponse(
    val id: UUID,
    val principalType: String,
    val fullName: String? = null,
    val tenantId: UUID,
    val tenantSlug: String,
    val tenantName: String,
    val status: String,
    val roles: List<String>,
    val modules: List<String>,
    val createdAt: Instant,

    val email: String? = null,
    val mustChangePassword: Boolean? = null,
    val lastLoginAt: Instant? = null,

    val name: String? = null,
    val description: String? = null
)