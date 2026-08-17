package br.com.deltaglobalbank.identity.features.auth.logout

data class LogoutRequest(
    val refreshToken: String? = null
)