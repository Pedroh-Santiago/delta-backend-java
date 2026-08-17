package br.com.deltaglobalbank.identity.features.users.me

sealed class MeException(message: String) : RuntimeException(message)

class PrincipalNotFoundException : MeException("principal_not_found")
class UnsupportedPrincipalTypeException(val type: String) :
    MeException("unsupported_principal_type")