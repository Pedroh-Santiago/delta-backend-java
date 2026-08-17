package br.com.deltaglobalbank.internal_treasury.domain.makePix

interface PixGateway {
    fun transferPix (
        accountId : Long,
        recipientInstitutionCode : String,
        recipientBranchCode : String,
        recipientAccountNumber : String,
        recipientAccountType : String,
        recipientName : String,
        operationAmount : Long,
    ): Long
}