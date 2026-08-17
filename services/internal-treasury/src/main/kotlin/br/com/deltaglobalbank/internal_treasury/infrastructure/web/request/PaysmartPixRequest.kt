package br.com.deltaglobalbank.internal_treasury.infrastructure.web.request

import java.math.BigDecimal

data class PaysmartPixRequest (
    val accountId: Long,
    val recipientInstitutionCode: String,
    val recipientBranchCode: String,
    val recipientAccountNumber: String,
    val recipientAccountType: String,
    val recipientName: String,
    val operationAmount: BigDecimal
)