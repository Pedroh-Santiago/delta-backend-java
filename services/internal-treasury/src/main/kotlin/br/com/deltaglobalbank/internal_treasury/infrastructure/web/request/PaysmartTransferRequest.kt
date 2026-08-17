package br.com.deltaglobalbank.internal_treasury.infrastructure.web.request

import com.fasterxml.jackson.annotation.JsonProperty

data class PaysmartTransferRequest(
    @JsonProperty("recipientAccountId")
    val accountNumber: Long,

    @JsonProperty("transferAmount")
    val amount: Int,

    @JsonProperty("freeDescription")
    val description: String
)