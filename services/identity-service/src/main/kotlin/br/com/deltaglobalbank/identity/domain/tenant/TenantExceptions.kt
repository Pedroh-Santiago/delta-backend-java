package br.com.deltaglobalbank.identity.domain.tenant

sealed class TenantDomainException(message: String) : RuntimeException(message)

class SlugAlreadyExistsException : TenantDomainException("slug_already_exists")