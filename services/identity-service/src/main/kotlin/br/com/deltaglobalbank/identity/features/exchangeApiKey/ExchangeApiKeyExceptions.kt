package br.com.deltaglobalbank.identity.features.exchangeApiKey

sealed class ExchangeApiKeyException(message: String) : RuntimeException(message)

class InvalidApiKeyException : ExchangeApiKeyException("invalid_api_key")
class ApiClientSuspendedForExchangeException : ExchangeApiKeyException("api_client_suspended")
class TenantInactiveForExchangeException : ExchangeApiKeyException("tenant_inactive")
class ApiKeyRequiredException : ExchangeApiKeyException("api_key_required")