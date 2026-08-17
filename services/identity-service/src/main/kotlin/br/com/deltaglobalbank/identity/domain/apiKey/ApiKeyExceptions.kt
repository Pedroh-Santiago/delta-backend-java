package br.com.deltaglobalbank.identity.domain.apiKey

sealed class ApiKeyDomainException(message: String) : RuntimeException(message)

class ApiClientNotFoundException : ApiKeyDomainException("api_client_not_found")
class ApiClientSuspendedException : ApiKeyDomainException("api_client_suspended")
class TenantInactiveForApiKeyException : ApiKeyDomainException("tenant_inactive")
class ApiKeyNotFoundException : ApiKeyDomainException("api_key_not_found")