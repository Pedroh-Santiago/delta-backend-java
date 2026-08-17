package br.com.deltaglobalbank.identity.features.assignUserRole

import br.com.deltaglobalbank.identity.domain.role.RoleCode
import java.util.UUID

data class AssignRolesResponse(
    val userId : UUID,
    val addedRoles : List<RoleCode>,
    val currentRoles : List<RoleCode>
) {
}