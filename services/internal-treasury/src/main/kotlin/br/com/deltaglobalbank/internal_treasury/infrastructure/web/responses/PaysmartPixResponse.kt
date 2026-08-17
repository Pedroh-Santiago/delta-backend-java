package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses

data class PaysmartPixResponse (
    val message: String,
    val idTransaction: Long,
    val scheduledTransactionId: Long,
    val authenticationCode: String,
    val arbiPixTransferError: ArbiPixTransferError? = null
)

data class ArbiPixTransferError (
    val idOrdemPagamento: Int,
    val endToEnd: String,
    val statusOrdemPagamento: String,
    val erros: List<ArbiPixDescriptionErro>? = null
)

data class ArbiPixDescriptionErro (
    val codigoErro: String,
    val descricaoErro: String
)