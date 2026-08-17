package br.com.deltaglobalbank.identity.features.auth.changePassword

data class ChangePasswordResponse(
    val success: Boolean = true,
    val message: String = "Senha alterada com sucesso. Faça login novamente."
)