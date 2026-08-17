package br.com.deltaglobalbank.internal_treasury.domain.internalTransference

interface PaysmartGateway {
    fun transfer(
        payerAccountNumber : Long,
        recipientAccountNumber : Long,
        amount : Int,
        description : String
    ): Long
}