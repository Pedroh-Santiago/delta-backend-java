package br.com.deltaglobalbank.sharedauth

class JwtValidationException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
