package br.com.deltaglobalbank.identity.features.auth.login

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,           // segundos
    val mustChangePassword: Boolean
)