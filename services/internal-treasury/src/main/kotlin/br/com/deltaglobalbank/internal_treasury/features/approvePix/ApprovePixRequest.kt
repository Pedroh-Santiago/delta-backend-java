package br.com.deltaglobalbank.internal_treasury.features.approvePix

import java.util.UUID

data class ApprovePixRequest (
    val approved: List<UUID>
)