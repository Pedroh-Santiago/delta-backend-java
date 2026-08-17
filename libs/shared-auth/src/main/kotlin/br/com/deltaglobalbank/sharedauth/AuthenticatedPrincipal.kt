package br.com.deltaglobalbank.sharedauth

import java.util.UUID

data class AuthenticatedPrincipal(
    val subject: UUID,
    val tenantId: UUID,
    val principalType: String,
    val roles: List<String>,
    val modules: List<String>,
    val mustChangePassword: Boolean,
    val jti: UUID
) {
    val isUser: Boolean get() = principalType == "user"
    val isApiClient: Boolean get() = principalType == "api_client"
}
