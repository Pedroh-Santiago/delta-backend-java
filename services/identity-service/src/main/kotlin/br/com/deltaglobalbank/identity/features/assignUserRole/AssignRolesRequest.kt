package br.com.deltaglobalbank.identity.features.assignUserRole

import br.com.deltaglobalbank.identity.domain.role.RoleCode


data class AssignRolesRequest(
    val rolesCodes: List<RoleCode>
)
