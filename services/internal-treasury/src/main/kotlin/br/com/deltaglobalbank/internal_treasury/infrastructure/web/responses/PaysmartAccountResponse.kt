package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses

data class PaysmartAccountResponse (
    val accountId: Long,
    val personalName: String? = null,
    val email: String? = null,
    val personalDocument: String,
    val personId: Long? = null,
    val productId: Int? = null,
    val accountNumber: Long,
    val status: Int? = null,
    val statusDescription: String? = null,
    val mainAccountId: Long? = null,
    val arrangementType: String? = null,
    val accountName: String? = null,
    val transactionInfo: Any? = null
)