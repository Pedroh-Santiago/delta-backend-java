package br.com.deltaglobalbank.identity.features.auth.refresh

data class RefreshTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val mustChangePassword: Boolean = false,
)