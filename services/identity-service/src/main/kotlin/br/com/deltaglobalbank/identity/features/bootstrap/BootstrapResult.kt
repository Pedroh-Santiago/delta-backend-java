package br.com.deltaglobalbank.identity.features.bootstrap

import java.util.UUID

data class BootstrapResult(
    val tenantId: UUID,
    val tenantSlug: String,
    val adminEmail: String,
    val temporaryPassword: String,
    val signingKeyId: String
)