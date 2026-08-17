package br.com.deltaglobalbank.identity.features.roles.listRoles

data class ListRolesResponse(
    val items: List<ListedRole>
)

data class ListedRole(
    val code: String,
    val name: String,
    val description: String?
)
