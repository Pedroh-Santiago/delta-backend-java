package br.com.deltaglobalbank.identity.domain.token

sealed class TokenDomainException(message: String) : RuntimeException(message)

class InvalidRefreshTokenException : TokenDomainException("invalid_refresh_token")
class RefreshTokenReuseDetectedException : TokenDomainException("refresh_token_reuse_detected")
class MissingRefreshTokenException : TokenDomainException("missing_refresh_token")
