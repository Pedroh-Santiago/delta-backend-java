package br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece

import java.util.UUID

data class ApproveInternalTransferenceRequest (
    val approved: List<UUID>
)