package br.com.deltaglobalbank.identity.features.users.createUser

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.user.Password
import java.time.Instant
import java.util.UUID

data class CreateUserResponse(
    val user: CreatedUser,
    val temporaryPassword: Password
)

data class CreatedUser(
    val id: UUID,
    val fullName: String,
    val email: String,
    val tenantId: UUID,
    val roles: List<RoleCode>,
    val mustChangePassword: Boolean,
    val createdAt: Instant
)