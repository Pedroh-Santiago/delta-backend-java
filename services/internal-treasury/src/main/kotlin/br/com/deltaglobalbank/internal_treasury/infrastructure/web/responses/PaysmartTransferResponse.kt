package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaysmartTransferResponse (
    val message: String,
    val authenticationCode: String,
    val freeField: String,
    val transactionId: Long,
    val dateTimeTransfer: String,
    val amount: Int,
    val recipientAccountHolder: String,
    val accountNumber: Long,
    val sendersAccountNumber: Int,
    val sendersName: String
)