package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference

import jakarta.validation.constraints.NotNull

data class OrderInternalTransferenceRequest (
    @field:NotNull(message = "CPF do Pagador é obrigatório")
    val payerCpf: String,
    @field:NotNull(message = "CPF do Recebedor é obrigatório")
    val receiverCpf: String,
    @field:NotNull(message = "Valor é obrigatório")
    val amount: Int,
    val description: String
)