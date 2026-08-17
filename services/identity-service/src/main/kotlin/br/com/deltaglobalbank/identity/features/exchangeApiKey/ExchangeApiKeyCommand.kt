package br.com.deltaglobalbank.identity.features.exchangeApiKey

data class ExchangeApiKeyCommand(
    val apiKey: String,
    val sourceIp: String,
    val userAgent: String
)

data class ExchangeApiKeyResult(
    val internalToken: String,
    val expiresAtEpochSeconds: Long,
    val principalId: String,
    val tenantId: String
)