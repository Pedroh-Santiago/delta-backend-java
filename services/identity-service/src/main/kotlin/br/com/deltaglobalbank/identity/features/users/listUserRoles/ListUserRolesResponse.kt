package br.com.deltaglobalbank.identity.features.users.listUserRoles

import java.time.Instant
import java.util.UUID

data class ListUserRolesResponse (
    val items: List<ListedUserRoles>
)

data class ListedUserRoles(
    val roleCode: String,
    val roleId: UUID,
    val moduleCode: String?,
    val grantedAt: Instant,
    val grantedBy: UUID?
)