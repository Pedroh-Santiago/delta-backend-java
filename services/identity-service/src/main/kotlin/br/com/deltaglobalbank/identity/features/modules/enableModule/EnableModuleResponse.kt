package br.com.deltaglobalbank.identity.features.modules.enableModule

import java.time.Instant

data class EnableModuleResponse(
    val moduleCode: String,
    val enabled: Boolean,
    val enabledAt: Instant,
) {
}