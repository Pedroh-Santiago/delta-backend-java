package br.com.deltaglobalbank.identity.domain.token

class SigningKeyNotFoundException : RuntimeException("signing_key_not_found")
class CannotRevokeActiveKeyException : RuntimeException("cannot_revoke_active_key")