package br.com.deltaglobalbank.internal_treasury.features.makePix

data class MakePixRequest (
    val accountId: Long,
    val recipientInstitutionCode: String,
    val recipientBranchCode: String,
    val recipientAccountNumber: String,
    val recipientAccountType: String,
    val recipientName: String,
    val operationAmount: Long
)