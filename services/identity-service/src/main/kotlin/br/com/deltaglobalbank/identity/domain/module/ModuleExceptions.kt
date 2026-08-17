package br.com.deltaglobalbank.identity.domain.module

sealed class ModuleDomainException(message: String) : RuntimeException(message)

class ModuleNotFoundException : ModuleDomainException("module_not_found")